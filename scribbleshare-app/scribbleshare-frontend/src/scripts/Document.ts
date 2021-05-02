import {Canvas} from "./canvas/Canvas.js";
import Client from './Client.js'
import socket from './protocol/WebSocketHandler.js'
import * as User from "./User.js";
import ClientMessageOpenDocument from "./protocol/client/messages/ClientMessageOpenDocument.js";
import ClientMessageCreateDocument from "./protocol/client/messages/ClientMessageCreateDocument.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";
import ClientMessageHandshake from "./protocol/client/messages/ClientMessageHandshake.js";
import invite from "./Invite.js";
import ClientMessageUpdateDocument from "./protocol/client/messages/ClientMessageUpdateDocument.js";
import ClientMessageDeleteDocument from "./protocol/client/messages/ClientMessageDeleteDocument.js";
import Sidebar, {SidebarItem} from "./Sidebar.js";
import ServerMessageUpdateDocument from "./protocol/server/messages/ServerMessageUpdateDocument";
import ServerMessageAddClient from "./protocol/server/messages/ServerMessageAddClient";
import ServerMessageRemoveClient from "./protocol/server/messages/ServerMessageRemoveClient";
import ServerMessageOpenDocument from "./protocol/server/messages/ServerMessageOpenDocument";
import ServerMessageHandshake from "./protocol/server/messages/ServerMessageHandshake";
import ServerMessageMouseMove from "./protocol/server/messages/ServerMessageMouseMove";
import ServerMessageDeleteDocument from "./protocol/server/messages/ServerMessageDeleteDocument";
import ServerMessage from "./protocol/server/ServerMessage";
import SocketEventType from "./protocol/SocketEventType";

const documents = new Map();
export let activeDocument: Document | null = null;
export const clientsToolbar: HTMLElement = document.getElementById("clientsToolbar")!;
export let localClientId = 0;
export let localClient = null;
const updateBar = document.getElementById('sideBottom')!;
updateBar.style.visibility = 'hidden';
const renameInput = document.getElementById('renameInput') as HTMLInputElement;
const deleteButton = document.getElementById('deleteButton')!;
const sidebar = new Sidebar(document.getElementById('side')!);

export default class Document {
    clients: Map<number, Client>;
    name: string;
    id: bigint;
    sidebarItem: SidebarItem;
    canvas: Canvas;

    constructor(name: string, id: bigint) {
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

    rename(name: string) {
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

    update(serverMessageUpdateDocument: ServerMessageUpdateDocument) {
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

    addClient(client: Client) {
        this.clients.set(client.id, client);
        clientsToolbar.appendChild(client.icon);
    }

    removeClient(id: number) {
        let client = this.clients.get(id);
        if (client !== undefined) {
            client.icon.remove();
        }
        this.clients.delete(id);
    }
}

renameInput.addEventListener('input', (event) => {
    console.log(event);
    if (activeDocument !== null) {
        activeDocument.rename(renameInput.value);
    }
});

deleteButton.addEventListener('click', (event) => {
    if (activeDocument !== null) {
        activeDocument.delete();
    }
})

document.getElementById('add')!.addEventListener('click', () => {
    if (activeDocument != null) activeDocument.close();
    socket.send(new ClientMessageCreateDocument());
});
socket.addMessageListener(ServerMessageType.ADD_CLIENT, (serverMessage: ServerMessage) => {
    let serverMessageAddClient = serverMessage as ServerMessageAddClient;
    serverMessageAddClient.clientUserTuples.forEach((clientUserTuple) => {
        let user = User.getUser(clientUserTuple[1]);
        if (user === undefined) {
            throw new Error('user is undefined');
        }
        let client = new Client(clientUserTuple[0], clientUserTuple[1]);
        if (client.id === localClientId) {
            // @ts-ignore todo
            localClient = client;
        }
        // @ts-ignore todo
        activeDocument.addClient(client);
        console.log('Add client ', client);
    })
});
socket.addMessageListener(ServerMessageType.REMOVE_CLIENT, (serverMessage: ServerMessage) => {
    let serverMessageRemoveClient = serverMessage as ServerMessageRemoveClient;
    console.log('Remove client ', serverMessageRemoveClient.id);
    // @ts-ignore todo
    activeDocument.removeClient(serverMessageRemoveClient.id)
});
socket.addMessageListener(ServerMessageType.OPEN_DOCUMENT, (serverMessage: ServerMessage) => {
    let serverMessageOpenDocument = serverMessage as ServerMessageOpenDocument;
    let document = documents.get(serverMessageOpenDocument.id);
    document.canvas.close();
    document.canvas = serverMessageOpenDocument.canvas;
    document.open();
})
socket.addMessageListener(ServerMessageType.UPDATE_DOCUMENT, (serverMessage: ServerMessage) => {
    let serverMessageUpdateDocument = serverMessage as ServerMessageUpdateDocument;
    let document = documents.get(serverMessageUpdateDocument.id);
    if (document === undefined) {
        new Document(serverMessageUpdateDocument.name + (serverMessageUpdateDocument.shared ? "(shared)" : ""), serverMessageUpdateDocument.id);
    } else {
        document.update(serverMessageUpdateDocument);
    }
});
socket.addMessageListener(ServerMessageType.HANDSHAKE, (serverMessage: ServerMessage) => {
    let serverMessageHandshake = serverMessage as ServerMessageHandshake;
    localClientId = serverMessageHandshake.client;
})
socket.addMessageListener(ServerMessageType.MOUSE_MOVE, (serverMessage: ServerMessage) => {
    let serverMessageMouseMove = serverMessage as ServerMessageMouseMove;
    // @ts-ignore todo
    let client = activeDocument.clients.get(serverMessageMouseMove.client);
    if (client === undefined) {
        throw new Error('client is undefined'); //todo
    }
    client.mouseMoves = serverMessageMouseMove.mouseMoves;
    client.time = 0;
    client.first = true;
})
socket.addEventListener(SocketEventType.OPEN, () => {
    socket.send(new ClientMessageHandshake(invite.getInvite()));
});
socket.addMessageListener(ServerMessageType.DELETE_DOCUMENT, (serverMessage: ServerMessage) => {
    let serverMessageDeleteDocument = serverMessage as ServerMessageDeleteDocument;
    let document = documents.get(serverMessageDeleteDocument.id);
    if (document === undefined) {
        return;
    }

    document.remove();
});