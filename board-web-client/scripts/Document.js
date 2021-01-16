export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

import LocalClient from './LocalClient.js';
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './WebSocketHandler.js'

const documents = new Map();
var activeDocument = null;

export default class Document {
    constructor(name, id, points) {
        this.name = name;
        this.sidebarItem = new SidebarItem(this.name, () => this.open());
        if (id != null) {
            this.id = id;
            documents.set(this.id, this);
        }
        this.points = points;
    }

    open() {
        activeDocument = this;
        socket.sendOpen(this.id);
    }

    draw(dt) {
        console.log('drawing ');
    }
}

var next = 0;
function newDocument() {
    return new Document('Untitled ' + next++);
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
    let doc = new Document(event.name, event.id);
    //window.history.pushState(doc.name, document.title, '/d/' + id);
});
socket.addEventListener('socketopen', (event) => {
    Board.inviteButton.innerHTML = 'invite';//todo abstract invitebutton to class???????
    var invite = document.location.href.substring(document.location.href.lastIndexOf("/") + 1);
    if (invite === '') {
        socket.sendCreate();
    } else {
        socket.sendOpen(invite);
    }
});

activeDocument = newDocument();
const localClient = new LocalClient();