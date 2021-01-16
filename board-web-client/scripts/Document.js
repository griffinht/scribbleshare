export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

import LocalClient from './LocalClient.js';
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './WebSocketHandler.js'

const clients = new Map();
const localClient = new LocalClient();
var activeDocument;

{
    window.addEventListener('resize', resizeCanvas);
    function resizeCanvas() {
        let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        canvas.width = canvas.parentElement.offsetWidth;
        canvas.height = canvas.parentElement.offsetHeight;
        ctx.putImageData(imageData, 0, 0);
        //todo redraw?
    };
    resizeCanvas();
}

var last = performance.now();
function draw(now) {
    let dt = (now - last);
    last = now;

    activeDocument.draw(dt);

    window.requestAnimationFrame(draw);
}

var next = 0;
function newDocument() {
    new Document('Untitled ' + next++);
}

document.getElementById('add').addEventListener('click', newDocument);

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
socket.addEventListener('open', (event) => {
    let doc = new Document(event.id, event.name);
    //window.history.pushState(doc.name, document.title, '/d/' + id);
    Board.inviteButton.innerHTML = 'invite';//todo abstract invitebutton to class???????
    var invite = document.location.href.substring(document.location.href.lastIndexOf("/") + 1);
    if (invite === '') {
        socket.sendCreate();
    } else {
        socket.sendOpen(invite);
    }

});


const documents = new Map();

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
    }

    draw(dt) {
        console.log('drawing ');
    }
}