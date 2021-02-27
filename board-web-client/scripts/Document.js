export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

import LocalClient from './LocalClient.js';
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './WebSocketHandler.js'
import * as User from "./User.js";

const documents = new Map();
var activeDocument = null;

class Document {
    constructor(name, id) {
        this.clients = new Map();
        this.name = name;
        this.id = id;
        this.sidebarItem = new SidebarItem(this.name, () => {
            if (this.id != null) {
                if (activeDocument != null) activeDocument.close();
                socket.sendOpen(this.id)
            } else {
                console.log('id was null', this);
            }
        });
        documents.set(this.id, this);
        this.points = {};
    }

    open() {
        activeDocument = this;
        console.log('opened ' + this.name);
        this.sidebarItem.setActive(false);
        window.history.pushState(document.name, document.title, '/d/' + this.id);
        this.points.forEach((points, id) => {
            ctx.beginPath();
            points.forEach((point) => {
                if (point.dt === 0) {
                    ctx.stroke();//todo only do this at the end
                    ctx.moveTo(point.x, point.y);
                } else {
                    ctx.lineTo(point.x, point.y);
                }
            })
            ctx.stroke();
        });
    }

    close() {
        localClient.update();
        ctx.clearRect(0, 0, canvas.width, canvas.height);//todo a loading screen?
    }

    draw(dt) {
        this.clients.forEach((client) => {
            client.draw(dt);
        })
    }
}

document.getElementById('add').addEventListener('click', () => {
    if (activeDocument != null) activeDocument.close();
    socket.sendCreate();
});

window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
    let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    let rect = canvas.parentNode.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    ctx.putImageData(imageData, 0, 0);
    //todo redraw?
}
resizeCanvas();

let last = performance.now();

function draw(now) {
    let dt = (now - last);
    last = now;

    if (activeDocument != null) {
        activeDocument.draw(dt);
    }

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);

socket.addEventListener('protocol.addclient', (event) => {
    let client = new Client(event.id, User.getUser(event.userId));
    activeDocument.clients.set(client.id, client);
    console.log('Add client ', client);
});
socket.addEventListener('protocol.removeclient', (event) => {
    console.log('Remove client ', activeDocument.clients.delete(event.id));
});
socket.addEventListener('protocol.draw', (event) => {
    let client = activeDocument.clients.get(event.id);
    console.log(activeDocument.clients, event.id);
    event.points.forEach((point) => {
        client.points.push(point);
    });
});
socket.addEventListener('protocol.adddocument', (event) => {
    documents.set(event.id, new Document(event.name, event.id));
});
socket.addEventListener('protocol.opendocument', (event) => {
    if (activeDocument != null) {
        activeDocument.close();
    }

    activeDocument = documents.get(event.document.id);
    Object.assign(activeDocument, event.document);
    activeDocument.open();

});
socket.addEventListener('protocol.handshake', (event) => {
    if (event.token != null) {
        window.localStorage.setItem('token', event.token.toString());
    }
})
socket.addEventListener('socket.open', () => {
    let token = window.localStorage.getItem('token');
    if (token != null) {
        token = BigInt(window.localStorage.getItem('token'));
    } else {
        token = BigInt(0);
    }
    socket.sendHandshake(token);
    let invite = document.location.href.substring(document.location.href.lastIndexOf("/") + 1);
    if (invite !== '') {
        socket.sendOpen(invite);
    }
});

const localClient = new LocalClient();