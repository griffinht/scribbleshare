export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

import LocalClient from './LocalClient.js';
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './WebSocketHandler.js'
import './InviteButton.js'

const documents = new Map();
const pendingDocuments = new Map();
var activeDocument = null;

export default class Document {
    constructor(name, id, points) {
        this.name = name;
        this.sidebarItem = new SidebarItem(this.name, () => {
            if (this.id != null) {
                socket.sendOpen(this.id)
            } else {
                console.log('id was null', this);
            };
        });
        if (id != null) {
            this.id = id;
            documents.set(this.id, this);
        } else {
            pendingDocuments.set(this.name, this);
        }
        this.points = points;
        //if (activeDocument == null) {
        //    this.open();
        //}
    }

    open() {
        activeDocument = this;
        console.log('opened ' + this.name);
        this.sidebarItem.setActive(false);
        window.history.pushState(document.name, document.title, '/d/' + this.id);
        //todo canvas stuff
    }

    draw(dt) {

    }
}

var next = 0;
function newDocument() {
    let d = new Document('Untitled ' + next++);
    socket.sendCreate(d.name);
}

document.getElementById('add').addEventListener('click', newDocument);

window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
    let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = canvas.parentElement.offsetHeight;
    ctx.putImageData(imageData, 0, 0);
    //todo redraw?
};
resizeCanvas();

var last = performance.now();
function draw(now) {
    let dt = (now - last);
    last = now;

    if (activeDocument != null) {
        activeDocument.draw(dt);
    }

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);

const clients = new Map();
socket.addEventListener('addclient', (event) => {
    let client = new Client(event.id);
    clients.set(client.id, client);
    console.log('Add client ', client);
});
socket.addEventListener('removeclient', (event) => {
    console.log('Remove client ', clients.delete(event.id));
});
socket.addEventListener('draw', (event) => {
    let client = clients.get(event.id);
    event.points.forEach(point => {//todo styling (point) => {} or point => {}
        client.points.push(point);
    });
});
console.log(socket);
socket.addEventListener('open', (event) => {
    let doc = pendingDocuments.get(event.name);
    if (doc != null) {
        doc.id = event.id;
        documents.set(doc.name, doc);
    } else {
        doc = new Document(event.name, event.id);
    }
    doc.open();
});
socket.addEventListener('socketopen', (event) => {
    var invite = document.location.href.substring(document.location.href.lastIndexOf("/") + 1);
    if (invite === '') {
        newDocument();
    } else {
        socket.sendOpen(invite);
    }
});

const localClient = new LocalClient();