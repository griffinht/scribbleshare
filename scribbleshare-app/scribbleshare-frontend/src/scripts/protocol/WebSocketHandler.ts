import ServerMessageType, {getServerMessage} from "./server/ServerMessageType.js";
import Environment from "../Environment.js";
import ServerMessage from "./server/ServerMessage.js";
import ByteBuffer from "./ByteBuffer.js";
import ClientMessage from "./client/ClientMessage.js";
import SocketEventType from "./SocketEventType.js";

class WebSocketHandler {
    messageQueue: ClientMessage[] = [];
    socketEventListeners: Map<SocketEventType, ((event: Event) => void)[]> = new Map();
    serverMessageListeners: Map<ServerMessageType, ((serverMessage: ServerMessage) => void)[]> = new Map();
    socket: WebSocket;

    constructor() {
        Object.keys(SocketEventType).forEach((key, value) => {
            this.socketEventListeners.set(value, []);
        });

        Object.keys(ServerMessageType).forEach((key, value) => {
            this.serverMessageListeners.set(value, []);
        });

        console.log('Opening WebSocket connection to ' + Environment.WEBSOCKET_HOST);
        this.socket = new WebSocket(Environment.WEBSOCKET_HOST);
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener('open', (event) => {
            console.log('WebSocket connection opened');
            this.dispatchEvent(SocketEventType.OPEN, event);
        });

        this.socket.addEventListener('close', (event) => {
            console.log('WebSocket connection closed');
            this.dispatchEvent(SocketEventType.CLOSE, event);
        });
    
        this.socket.addEventListener('message', (event) => {
            console.warn('recv');
            if (typeof event.data === 'string') {
                console.log(event.data);
            } else {
                let byteBuffer = new ByteBuffer(event.data);
                while (byteBuffer.hasNext()) {
                    this.dispatchMessageEvent(getServerMessage(byteBuffer.readUint8(), byteBuffer));
                }
            }
        });

        this.socket.addEventListener('error', (event) => {
            console.error('socket error', event);
        });
    }

    addEventListener(type: SocketEventType, onEvent: (event: Event) => void) {
        this.socketEventListeners.get(type)!.push(onEvent);
    }
    
    dispatchEvent(type: SocketEventType, event: Event) {//todo improve
        console.log('recv', type, event);
        this.socketEventListeners.get(type)!.forEach(onEvent => onEvent(event));
    }

    addMessageListener(type: ServerMessageType, onMessageEvent: (serverMessage: ServerMessage) => void) {
        this.serverMessageListeners.get(type)!.push(onMessageEvent);
    }

    dispatchMessageEvent(serverMessage:  ServerMessage) {
        console.log('recv', serverMessage.type, serverMessage);
        this.serverMessageListeners.get(serverMessage.type)!.forEach(onServerMessage => onServerMessage(serverMessage));
    }

    queue(clientMessage: ClientMessage) {
        this.messageQueue.push(clientMessage);
    }

    flush() {
        this.send(this.messageQueue);
        this.messageQueue.length = 0;
    }

    send(clientMessages: ClientMessage[] | ClientMessage) {
        if (!Array.isArray(clientMessages)) clientMessages = [clientMessages];
        if (clientMessages.length === 0) return;

        if (this.socket.readyState === WebSocket.OPEN) {
            let byteBuffer = new ByteBuffer();
            clientMessages.forEach((clientMessage) => {
                clientMessage.serialize(byteBuffer);
                console.log('send', clientMessage);
            });
            this.socket.send(byteBuffer.getBuffer());
        } else {
            console.error('tried to send messages while websocket was closed', clientMessages);
        }
    }
}

export default new WebSocketHandler();
