import {canvas, Canvas, ctx} from "./canvas/Canvas.js";
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './protocol/WebSocketHandler.js'
import * as User from "./User.js";
import ClientMessageOpenDocument from "./protocol/client/messages/ClientMessageOpenDocument.js";
import ClientMessageCreateDocument from "./protocol/client/messages/ClientMessageCreateDocument.js";
import ClientMessageHandshake from "./protocol/client/messages/ClientMessageHandshake.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";
import ClientMessageUpdateCanvas from "./protocol/client/messages/ClientMessageUpdateCanvas.js";
import Shape from "./canvas/canvasObjects/Shape.js";
import {CanvasObjectType} from "./canvas/CanvasObjectType.js";
import CanvasObjectWrapper from "./canvas/CanvasObjectWrapper.js";
import SocketEventType from "./protocol/SocketEventType.js";
import ClientMessageGetInvite from "./protocol/client/messages/ClientMessageGetInvite.js";

const documents = new Map();
let activeDocument = null;
export const clientsToolbar = document.getElementById("clientsToolbar");

class Document {
    constructor(name, id) {
        this.clients = new Map();
        this.name = name;
        this.id = id;
        this.sidebarItem = new SidebarItem(this.name, () => {
            if (this.id != null) {
                if (activeDocument != null) activeDocument.close();

                activeDocument = this;
                activeDocument.open();

                socket.send(new ClientMessageOpenDocument(this));
            } else {
                console.log('id was null', this);
            }
        });
        documents.set(this.id, this);
        this.canvas = new Canvas();
    }

    open() {
        activeDocument = this;
        inviteButton.style.visibility = 'visible';
        console.log('opened ' + this.name);
        this.sidebarItem.setActive(false);
        //window.history.pushState(document.name, document.title, '/d/' + this.id); todo
        this.canvas.draw(-1);
        //this.addClient(localClient);
    }

    close() {
        localUpdate();
        ctx.clearRect(0, 0, canvas.width, canvas.height);//todo a loading screen?
        this.clients.forEach((client) => {
            this.removeClient(client.id);
        })
    }

    addClient(client) {
        this.clients.set(client.id, client);
        clientsToolbar.appendChild(client.icon);
    }

    removeClient(id) {
        this.clients.get(id).icon.remove();
        this.clients.delete(id);
    }
}

document.getElementById('add').addEventListener('click', () => {
    if (activeDocument != null) activeDocument.close();
    socket.send(new ClientMessageCreateDocument());
});

window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
    let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    let rect = canvas.parentNode.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    ctx.putImageData(imageData, 0, 0);
    if (activeDocument != null) {
        activeDocument.canvas.resize();
    }
}
resizeCanvas();

let last = performance.now();

function draw(now) {
    let dt = (now - last);
    last = now;

    if (activeDocument != null) {
        activeDocument.canvas.draw(dt);
    }

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);

socket.addMessageListener(ServerMessageType.ADD_CLIENT, (serverMessageAddClient) => {
    serverMessageAddClient.clients.forEach((value) => {
        let client = new Client(value.id, User.getUser(value.userId));
        activeDocument.addClient(client);
        console.log('Add client ', client);
    })
});
socket.addMessageListener(ServerMessageType.REMOVE_CLIENT, (serverMessageRemoveClient) => {
    console.log('Remove client ', serverMessageRemoveClient.id);
    activeDocument.removeClient(serverMessageRemoveClient.id)
});
socket.addMessageListener(ServerMessageType.UPDATE_CANVAS, (serverMessageUpdateCanvas) => {
    if (activeDocument != null) {
        activeDocument.canvas.updateMultiple(serverMessageUpdateCanvas.canvasObjectWrappers);
    } else {
        console.warn('oops');
    }
});
socket.addMessageListener(ServerMessageType.UPDATE_DOCUMENT, (serverMessageUpdateDocument) => {
    console.log(serverMessageUpdateDocument.shared);
    documents.set(serverMessageUpdateDocument.id,
        new Document(serverMessageUpdateDocument.name + (serverMessageUpdateDocument.shared ? "(shared)" : ""),
            serverMessageUpdateDocument.id));
});
socket.addMessageListener(ServerMessageType.OPEN_DOCUMENT, (serverMessageOpenDocument) => {
    activeDocument.canvas = serverMessageOpenDocument.canvas;
})
socket.addEventListener(SocketEventType.OPEN, () => {
    let invite;
    let index = document.location.href.lastIndexOf('invite=');
    if (index === -1) {
        invite = '';
    } else {
        invite = document.location.href.substring(index + 7, index + 7 + 6);
    }
    socket.send(new ClientMessageHandshake(invite));
});

const inviteButton = document.getElementById("inviteButton");

inviteButton.addEventListener('click', (event) => {
    socket.send(new ClientMessageGetInvite());
})

socket.addMessageListener(ServerMessageType.GET_INVITE, (serverMessageGetInvite) => {
    window.alert('Join at localhost/index.html?invite=' + serverMessageGetInvite.code);
})

const MAX_TIME = 2000;
const UPDATE_INTERVAL = 1000;
let lastUpdate = 0;
let updateCanvas = new Canvas();
setInterval(localUpdate, UPDATE_INTERVAL);
function localUpdate() {
    lastUpdate = window.performance.now();
    if (updateCanvas.updateCanvasObjects.size > 0) {
        socket.send(new ClientMessageUpdateCanvas(updateCanvas.updateCanvasObjects));//todo breaks the server when the size is 0
        updateCanvas.updateCanvasObjects.clear();
    }
}

function getDt() {
    return (window.performance.now() - lastUpdate) / MAX_TIME * 255;
}

canvas.addEventListener('click', (event) => {
    let shape = Shape.create(event.offsetX, event.offsetY, 50, 50);
    let id = (Math.random() - 0.5) * 32000;
    updateCanvas.update(CanvasObjectType.SHAPE, id, CanvasObjectWrapper.create(getDt(), shape));
    activeDocument.canvas.insert(CanvasObjectType.SHAPE, id, shape);
})