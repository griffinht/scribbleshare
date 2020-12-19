export default class WebSocketHandler {
    constructor() {
        this.socket = new WebSocket('ws://localhost/websocket');

        this.socket.addEventListener('open', (event) => {
            console.log(this);
            console.log(event);
            this.socket.send('opened connection, handshaking');
        });

        this.socket.addEventListener('close', (event) => {
            console.log('close');
        });
    
        this.socket.addEventListener('message', (event) => {
            console.log(event.data);
        });

        this.socket.addEventListener('error', (event) => {
            console.log('error: ' + event);
        });
    }

    send(message) {
        console.log(this.socket);
    }
}