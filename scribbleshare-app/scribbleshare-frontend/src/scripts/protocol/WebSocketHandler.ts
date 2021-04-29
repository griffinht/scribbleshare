import BufferbyteBuffer from './BufferbyteBuffer.js'
import BufferbyteBuffer from "./BufferbyteBuffer.js";
import ServerMessageType, {getServerMessage} from "./server/ServerMessageType.js";
import SocketEventType from "./SocketEventType.js";
import Environment from "../Environment.js";

class WebSocketHandler {
    constructor() {
        this.messageQueue = [];
        this.socketEvents = {};
        Object.keys(SocketEventType).forEach((key, value) => {
            this.socketEvents[value] = [];
        });

        this.serverMessageEvents = {};
        Object.keys(ServerMessageType).forEach((key, value) => {
            this.serverMessageEvents[value] = [];
        });

        console.log('Opening WebSocket connection to ' + Environment.WEBSOCKET_HOST);
        this.socket = new WebSocket(Environment.WEBSOCKET_HOST);
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener('open', (event) => {
            console.log('WebSocket connection opened');
            this.dispatchEvent(SocketEventType.OPEN);
        });

        this.socket.addEventListener('close', (event) => {
            console.log('WebSocket connection closed');
            this.dispatchEvent(SocketEventType.CLOSE);
        });
    
        this.socket.addEventListener('message', (event) => {
            console.warn('recv');
            if (typeof event.data === 'string') {
                console.log(event.data);
            } else {
                let byteBuffer = new BufferbyteBuffer(event.data);
                while (byteBuffer.hasNext()) {
                    this.dispatchMessageEvent(getServerMessage(byteBuffer.readUint8(), byteBuffer));
                }
            }
        });

        this.socket.addEventListener('error', (event) => {
            console.error('socket error', event);
        });
    }

    addEventListener(socketEventType, onSocketEvent) {
        this.socketEvents[socketEventType].push(onSocketEvent);
    }
    
    dispatchEvent(socketEventType, socketEvent) {
        console.log('recv', socketEventType, socketEvent);
        this.socketEvents[socketEventType].forEach(onSocketEvent => onSocketEvent(socketEvent));
    }

    addMessageListener(serverMessageType, onServerMessage) {
        this.serverMessageEvents[serverMessageType].push(onServerMessage);
    }

    dispatchMessageEvent(serverMessage) {
        console.log('recv', serverMessage.type, serverMessage);
        this.serverMessageEvents[serverMessage.type].forEach(onServerMessage => onServerMessage(serverMessage));
    }

    queue(message) {
        this.messageQueue.push(message);
    }

    flush() {
        this.send(this.messageQueue);
        this.messageQueue.length = 0;
    }

    send(messages) {
        if (!Array.isArray(messages)) messages = [messages];
        if (messages.length === 0) return;

        if (this.socket.readyState === WebSocket.OPEN) {
            let byteBuffer = new BufferbyteBuffer();
            messages.forEach((message) => {
                message.serialize(byteBuffer);
                console.log('send', message);
            });
            this.socket.send(byteBuffer.getBuffer());
        } else {
            console.error('tried to send messages while websocket was closed', messages);
        }
    }
}

export default new WebSocketHandler();
