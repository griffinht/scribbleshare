import {Canvas} from "./canvas/Canvas.js";
import LocalClient from './LocalClient.js';
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './protocol/WebSocketHandler.js'
import * as User from "./User.js";
import ClientMessageOpenDocument from "./protocol/client/messages/ClientMessageOpenDocument.js";
import ClientMessageCreateDocument from "./protocol/client/messages/ClientMessageCreateDocument.js";
import ClientMessageHandshake from "./protocol/client/messages/ClientMessageHandshake.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";
import WebSocketHandlerType from "./protocol/WebSocketHandlerType.js";
import ClientMessageUpdateCanvas from "./protocol/client/messages/ClientMessageUpdateCanvas.js";

const UPDATE_INTERVAL = 1000;
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

                socket.send(new ClientMessageOpenDocument(this.id));
            } else {
                console.log('id was null', this);
            }
        });
        documents.set(this.id, this);
        this.canvas = new Canvas();
    }

    update() {

    }

    open() {
        activeDocument = this;
        inviteButton.style.visibility = 'visible';
        console.log('opened ' + this.name);
        this.sidebarItem.setActive(false);
        //window.history.pushState(document.name, document.title, '/d/' + this.id); todo
        this.canvas.draw(-1);
        this.addClient(localClient);
    }

    close() {
        localClient.update();
        this.canvas.clear();
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
        activeDocument.canvas.draw();
    }

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);

socket.addMessageListener(ServerMessageType.ADD_CLIENT, (event) => {
    let client = new Client(event.id, User.getUser(event.userId));
    activeDocument.addClient(client);
    console.log('Add client ', client);
});
socket.addMessageListener(ServerMessageType.REMOVE_CLIENT, (event) => {
    console.log('Remove client ', activeDocument.removeClient(event.id));
});
socket.addMessageListener(ServerMessageType.UPDATE_CANVAS, (event) => {
    if (activeDocument != null) {
        event.canvasMap.forEach((value, key) => {
            activeDocument.canvas.update(value.canvasObjects);
        })
    } else {
        console.warn('oops');
    }
});
socket.addMessageListener(ServerMessageType.ADD_DOCUMENT, (event) => {
    documents.set(event.id, new Document(event.name, event.id));
});
socket.addMessageListener(ServerMessageType.HANDSHAKE, (event) => {
    window.localStorage.setItem('token', event.token.toString());
})
socket.addMessageListener(ServerMessageType.OPEN_DOCUMENT, (event) => {

})
socket.addEventListener(WebSocketHandlerType.OPEN, () => {
    let token = window.localStorage.getItem('token');
    if (token != null) {
        token = BigInt(window.localStorage.getItem('token'));
    } else {
        token = BigInt(0);
    }
    socket.send(new ClientMessageHandshake(token));
    let invite = document.location.href.substring(document.location.href.lastIndexOf("/") + 1);
    if (invite !== '') {
        try {
            let bigint = BigInt(invite);
            //socket.sendOpen(bigint); todo
        } catch(e) {
            console.error('improper invite', invite);
        }
    }
});

const localClient = new LocalClient();

const inviteButton = document.getElementById("inviteButton");

inviteButton.addEventListener('click', (event) => {
    console.log('invite');
})
/*
setInterval(() => {
    if (activeDocument != null) {
        activeDocument.update()
    }
}, UPDATE_INTERVAL);*/
