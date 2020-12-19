export default class WebSocketHandler {
    constructor() {
        this.socket = new WebSocket('ws://localhost/websocket');
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener('open', (event) => {
            console.log(this);
            console.log(event);
            this.socket.send('opened connection, handshaking');
        });

        this.socket.addEventListener('close', (event) => {
            console.log('close');
        });
    
        this.socket.addEventListener('message', (event) => {
            if (typeof event.data === 'string') {
                console.log(event.data);
            } else {
                let dataView = new DataView(event.data);
                let offset = 0;

                while (true) {
                    let type = dataView.getUint8(offset);
                    offset += 1;
                    console.log(type);
                    break;
                    switch (type) {
                        case 0:
                            return;
                        case 1: //draw
                            offset += 1 + 2 + 2;
                            console.log(ctx);
                            break;
                        case 2: //offset draw
                            offset += 1 + 2 + 2;
                            console.log(ctx);
                        default:
                            console.error('unknown payload type ' + type + ', offset ' + offset + ', event ' + event);
                    }
                }
            }
        });

        this.socket.addEventListener('error', (event) => {
            console.log('error: ' + event);
        });
    }

    send(payload) {
        if (this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(payload);
        } else {
            console.error('tried to send payload while websocket was closed' + payload);
        }
    }

    sendDraw(x, y) {
        let buffer = new ArrayBuffer(5);
        let dataView = new DataView(buffer);
        let offset = 0;

        dataView.setUint8(offset, 1);
        offset += 1;
        dataView.setInt16(offset, x);
        offset += 2;
        dataView.setInt16(offset, y);
        offset += 2;

        this.send(buffer);
    }

    sendOffsetDraw(offsetX, offsetY) {
        let buffer = new ArrayBuffer(5);
        let dataView = new DataView(buffer);
        let offset = 0;

        dataView.setUint8(offset, 2);
        offset += 1;
        dataView.setInt16(offset, offsetX);
        offset += 2;
        dataView.setInt16(offset, offsetY);
        offset += 2;

        this.send(buffer);
    }
}