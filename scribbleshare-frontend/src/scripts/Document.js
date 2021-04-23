import {Canvas} from "./canvas/Canvas.js";
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './protocol/WebSocketHandler.js'
import * as User from "./User.js";
import ClientMessageOpenDocument from "./protocol/client/messages/ClientMessageOpenDocument.js";
import ClientMessageCreateDocument from "./protocol/client/messages/ClientMessageCreateDocument.js";
import ClientMessageHandshake from "./protocol/client/messages/ClientMessageHandshake.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";
import SocketEventType from "./protocol/SocketEventType.js";
import ClientMessageGetInvite from "./protocol/client/messages/ClientMessageGetInvite.js";

const documents = new Map();
export let activeDocument = null;
export const clientsToolbar = document.getElementById("clientsToolbar");
let localClientId = 0;
let localClient = null;

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
        this.canvas.open();
        inviteButton.style.visibility = 'visible';
        console.log('opened ' + this.name);
        this.sidebarItem.setActive(false);
        //window.history.pushState(document.name, document.title, '/d/' + this.id); todo
        //this.addClient(localClient);
        //todo
        /*let request = new XMLHttpRequest();
        request.responseType = 'arraybuffer';
        request.addEventListener('load', (event) => {
            if (request.status !== 200) {
                console.error(request.status + ' while fetching document');
                return;
            }
            this.canvas = new Canvas(new BufferReader(new Uint8Array(request.response).buffer));
            console.log('GET', this.canvas);
        });
        request.open('GET', apiUrl + '/document/' + this.id);
        request.send();*/
    }

    close() {
        this.canvas.close();
        //todo a loading screen?
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
socket.addMessageListener(ServerMessageType.ADD_CLIENT, (serverMessageAddClient) => {
    serverMessageAddClient.clients.forEach((value) => {
        let client = new Client(value.id, User.getUser(value.userId));
        if (client.id === localClientId) {
            localClient = client;
        }
        activeDocument.addClient(client);
        console.log('Add client ', client);
    })
});
socket.addMessageListener(ServerMessageType.REMOVE_CLIENT, (serverMessageRemoveClient) => {
    console.log('Remove client ', serverMessageRemoveClient.id);
    activeDocument.removeClient(serverMessageRemoveClient.id)
});
socket.addMessageListener(ServerMessageType.OPEN_DOCUMENT, (serverMessageOpenDocument) => {
    let document = documents.get(serverMessageOpenDocument.id);
    document.canvas = serverMessageOpenDocument.canvas;
    document.open();
})
socket.addMessageListener(ServerMessageType.UPDATE_DOCUMENT, (serverMessageUpdateDocument) => {
    documents.set(serverMessageUpdateDocument.id, new Document(serverMessageUpdateDocument.name + (serverMessageUpdateDocument.shared ? "(shared)" : ""), serverMessageUpdateDocument.id));
});
socket.addMessageListener(ServerMessageType.HANDSHAKE, (serverMessageHandshake) => {
    localClientId = serverMessageHandshake.client;
})
socket.addMessageListener(ServerMessageType.MOUSE_MOVE, (serverMessageMouseMove) => {
    activeDocument.clients.get(serverMessageMouseMove.id).mouseMoves = serverMessageMouseMove.mouseMoves;
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
