import {Canvas} from "./Canvas.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

import LocalClient from './LocalClient.js';
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './WebSocketHandler.js'
import * as User from "./User.js";

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

                activeDocument = this.id;
                activeDocument.open();

                socket.sendOpen(this.id);
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
        activeDocument.canvas.draw(dt);
    }

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);

socket.addEventListener('protocol.addClient', (event) => {
    let client = new Client(event.id, User.getUser(event.userId));
    activeDocument.addClient(client);
    console.log('Add client ', client);
});
socket.addEventListener('protocol.removeClient', (event) => {
    console.log('Remove client ', activeDocument.removeClient(event.id));
});
socket.addEventListener('protocol.updateDocument', (event) => {
    Object.assign(documents.get(event.document.id), event.document);
});
socket.addEventListener('protocol.addDocument', (event) => {
    documents.set(event.id, new Document(event.name, event.id));
});
socket.addEventListener('protocol.handshake', (event) => {
    window.localStorage.setItem('token', event.token.toString());
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

})