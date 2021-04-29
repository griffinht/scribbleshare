import {Canvas} from "./canvas/Canvas.js";
import Client from './Client.js'
import socket from './protocol/WebSocketHandler.js'
import * as User from "./User.js";
import ClientMessageOpenDocument from "./protocol/client/messages/ClientMessageOpenDocument.js";
import ClientMessageCreateDocument from "./protocol/client/messages/ClientMessageCreateDocument.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";
import SocketEventType from "./protocol/SocketEventType.js";
import ClientMessageHandshake from "./protocol/client/messages/ClientMessageHandshake.js";
import invite from "./Invite.js";
import ClientMessageUpdateDocument from "./protocol/client/messages/ClientMessageUpdateDocument.js";
import ClientMessageDeleteDocument from "./protocol/client/messages/ClientMessageDeleteDocument.js";
import Sidebar, {SidebarItem} from "./Sidebar.js";
import color from "./ColorSelector.js";
import shape from "./ShapeSelector.js";

const documents = new Map();
export let activeDocument: Document | null = null;
export const clientsToolbar = document.getElementById("clientsToolbar");
export let localClientId = 0;
export let localClient = null;
const updateBar = document.getElementById('sideBottom')!;
updateBar.style.visibility = 'hidden';
const renameInput = document.getElementById('renameInput');
const deleteButton = document.getElementById('deleteButton');
const sidebar = new Sidebar(document.getElementById('side')!);

export default class Document {
    clients: Map<number, Client>;
    name: string;
    id: bigint;
    sidebarItem: SidebarItem;
    canvas: Canvas;

    constructor(name, id) {
        this.clients = new Map();
        this.name = name;
        this.id = id;
        this.sidebarItem = sidebar.addItem(this.name, () => {
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

    rename(name) {
        this.name = name;
        this.sidebarItem.button.innerText = name;
        socket.send(new ClientMessageUpdateDocument(this));
    }

    delete() {
        socket.send(new ClientMessageDeleteDocument(this));
    }

    open() {
        activeDocument = this;
        this.canvas.open();
        invite.setVisible(true);
        console.log('opened ' + this.name);
        this.sidebarItem.setActive(false);
        renameInput.value = this.name;
        updateBar.style.visibility = 'visible';
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
            this.canvas = new Canvas(new BufferbyteBuffer(new Uint8Array(request.response).buffer));
            console.log('GET', this.canvas);
        });
        request.open('GET', apiUrl + '/document/' + this.id);
        request.send();*/
    }

    update(serverMessageUpdateDocument) {
        this.name = serverMessageUpdateDocument.name;
        this.sidebarItem.button.innerText = this.name;
    }

    remove() {
        this.close();
        this.sidebarItem.remove();
    }

    close() {
        this.canvas.close();
        invite.setVisible(false);
        updateBar.style.visibility = 'hidden';
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

renameInput.addEventListener('input', (event) => {
    console.log(event);
    if (activeDocument !== null) {
        activeDocument.rename(event.target.value);
    }
});

deleteButton.addEventListener('click', (event) => {
    if (activeDocument !== null) {
        activeDocument.delete();
    }
})

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
    document.canvas.close();
    document.canvas = serverMessageOpenDocument.canvas;
    document.open();
})
socket.addMessageListener(ServerMessageType.UPDATE_DOCUMENT, (serverMessageUpdateDocument) => {
    let document = documents.get(serverMessageUpdateDocument.id);
    if (document === undefined) {
        new Document(serverMessageUpdateDocument.name + (serverMessageUpdateDocument.shared ? "(shared)" : ""), serverMessageUpdateDocument.id);
    } else {
        document.update(serverMessageUpdateDocument);
    }
});
socket.addMessageListener(ServerMessageType.HANDSHAKE, (serverMessageHandshake) => {
    localClientId = serverMessageHandshake.client;
})
socket.addMessageListener(ServerMessageType.MOUSE_MOVE, (serverMessageMouseMove) => {
    let client = activeDocument.clients.get(serverMessageMouseMove.client);
    client.mouseMoves = serverMessageMouseMove.mouseMoves;
    client.time = 0;
    client.first = true;
})
socket.addEventListener(SocketEventType.OPEN, () => {
    socket.send(new ClientMessageHandshake(invite.getInvite()));
});
socket.addMessageListener(ServerMessageType.DELETE_DOCUMENT, (serverMessageDeleteDocument) => {
    let document = documents.get(serverMessageDeleteDocument.id);
    if (document === undefined) {
        return;
    }

    document.remove();
});