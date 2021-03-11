import BufferReader from './BufferReader.js'
import BufferWriter from "./BufferWriter.js";
import ServerMessageAddClient from "./server/messages/ServerMessageAddClient.js";
import ServerMessageRemoveClient from "./server/messages/ServerMessageRemoveClient.js";
import ServerMessageOpenDocument from "./server/messages/ServerMessageOpenDocument.js";
import ServerMessageUpdateDocument from "./server/messages/ServerMessageUpdateDocument.js";
import ServerMessageAddDocument from "./server/messages/ServerMessageAddDocument.js";
import ServerMessageHandshake from "./server/messages/ServerMessageHandshake.js";
import ServerMessageAddUser from "./server/messages/ServerMessageAddUser.js";
import ServerMessageType from "./server/ServerMessageType.js";
import WebSocketHandlerType from "./WebSocketHandlerType.js";

const HTTP_PORT = 8080;
const HTTPS_PORT = 443;

class WebSocketHandler {
    constructor() {
        this.events = {};
        Object.keys(WebSocketHandlerType).forEach((key, value) => {
            this.events[value] = [];
        });

        this.messageEvents = {};
        Object.keys(ServerMessageType).forEach((key, value) => {
            this.messageEvents[value] = [];
        });

        console.log(this.events, this.messageEvents);

        let webSocketUrl;
        if (window.location.protocol === 'https:') {
            webSocketUrl = 'wss://localhost:' + HTTPS_PORT + '/websocket';
        } else {
            console.warn('Insecure connection, this better be a development environment');//todo disable/disallow
            webSocketUrl = 'ws://localhost:' + HTTP_PORT + '/websocket';
        }
        console.log('Opening WebSocket connection to ' + webSocketUrl);
        this.socket = new WebSocket(webSocketUrl);
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener('open', (event) => {
            console.log('WebSocket connection opened');
            this.dispatchEvent(WebSocketHandlerType.OPEN);
        });

        this.socket.addEventListener('close', (event) => {
            console.log('WebSocket connection closed');
            this.dispatchEvent(WebSocketHandlerType.CLOSE);
        });
    
        this.socket.addEventListener('message', (event) => {
            if (typeof event.data === 'string') {
                console.log(event.data);
            } else {
                let reader = new BufferReader(event.data);
                while (reader.hasNext()) {
                    let type = reader.readUint8();
                    let message;
                    switch (type) {
                        case 0:
                            message = new ServerMessageAddClient(reader);
                            break;
                        case 1:
                            message = new ServerMessageRemoveClient(reader);
                            break;
                        case 2:
                            message = new ServerMessageUpdateDocument(reader);
                            break;
                        case 3:
                            message = new ServerMessageOpenDocument(reader);
                            break;
                        case 4:
                            message = new ServerMessageAddDocument(reader);
                            break;
                        case 5:
                            message = new ServerMessageHandshake(reader);
                            break;
                        case 6:
                            message = new ServerMessageAddUser(reader)
                            break;
                        default:
                            console.error('unknown payload type ' + type + ', offset ' + reader.position + ', event ', event);
                            break;
                    }
                    this.dispatchMessageEvent(message.type, message);
                }
            }
        });

        this.socket.addEventListener('error', (event) => {
            console.log('socket error', event);
        });
    }

    addEventListener(type, onevent) {
        this.events[type].push(onevent);
    }
    
    dispatchEvent(type, event) {
        console.log('recv', type, event);
        this.events[type].forEach(onevent => onevent(event));
    }

    addMessageListener(type, onevent) {
        this.messageEvents[type].push(onevent);
    }

    dispatchMessageEvent(type, event) {
        console.log(type);
        this.messageEvents[type].forEach(onevent => onevent(event));
    }

    send(message) {
        if (this.socket.readyState === WebSocket.OPEN) {
            console.log('send', message);
            let buffer = new ArrayBuffer(message.getBufferSize());
            message.serialize(new BufferWriter(buffer))
            this.socket.send(buffer);
        } else {
            console.error('tried to send message while websocket was closed', message);
        }
    }
}

export default new WebSocketHandler();
