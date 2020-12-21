import Client from './Client.js'

export default class WebSocketHandler {
    static UPDATE_INTERVAL = 1000/10;
    
    constructor() {
        this.socket = new WebSocket('ws://localhost/websocket');
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener('open', (event) => {
            console.log('WebSocket connection opened');
            this.sendOpen();
            setInterval(() => {
                let points  = localClient.getPoints();
                if (points.length === 0) {
                    return;
                }
                let buffer = new ArrayBuffer(1 + 1 + points.length * 5);
                let dataView = new DataView(buffer);
                let offset = 0;

                dataView.setUint8(offset, 1);
                offset += 1;

                dataView.setUint8(offset, points.length);
                offset += 1;

                points.forEach(point => {
                    dataView.setUint8(offset, point.dt);
                    offset += 1;
                    dataView.setInt16(offset, point.x);
                    offset += 2;
                    dataView.setInt16(offset, point.y);
                    offset += 2;
                });

                points.length = 0;//clear

                this.send(buffer);
            }, WebSocketHandler.UPDATE_INTERVAL);
        });

        this.socket.addEventListener('close', (event) => {
            console.log('WebSocket connection closed');
        });
    
        this.socket.addEventListener('message', (event) => {
            if (typeof event.data === 'string') {
                console.log(event.data);
            } else {
                let dataView = new DataView(event.data);
                let offset = 0;

                while (offset < dataView.byteLength) {
                    let type = dataView.getUint8(offset);
                    offset += 1;
                    console.log('got ' + type);
                    switch (type) {
                        case 0: {//add client
                            new Client(dataView.getUint16(offset));
                            offset += 2;
                            break;
                        }
                        case 1: {//remove client
                            clients.delete(dataView.getUint16(offset));
                            offset += 2;
                            break;
                        }
                        case 2: {//draw
                            let client = clients.get(dataView.getUint16(offset));
                            offset += 2;
                            let size = dataView.getUint8(offset);
                            offset += 1;
                            for (let i = 0; i < size; i++) {
                                let point = {};
                                point.dt = dataView.getUint8(offset);
                                offset += 1;
                                point.x = dataView.getInt16(offset);
                                offset += 2;
                                point.y = dataView.getInt16(offset);
                                offset += 2;
                                client.points.push(point);
                            }
                            break;
                        }
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
            console.log('sending');
            this.socket.send(payload);
        } else {
            console.error('tried to send payload while websocket was closed' + payload);
        }
    }

    sendOpen() {
        let buffer = new ArrayBuffer(1);
        let dataView = new DataView(buffer);
        let offset = 0;

        dataView.setUint8(offset, 0);
        offset += 1;

        this.send(buffer);
    }
}