import Client from './Client.js'
import Document from './Document.js'
import Sidebar from './Sidebar.js';

const UPDATE_INTERVAL = 1000;
var next = 0;
export default class WebSocketHandler {
    constructor() {
        Board.inviteButton.innerHTML = "connecting...";//todo add spinner

        this.socket = new WebSocket('ws://localhost/websocket');
        this.socket.binaryType = 'arraybuffer';

        document.getElementById('add').addEventListener('click', event => {
            this.sendCreate();
        });

        this.socket.addEventListener('open', (event) => {
            console.log('WebSocket connection opened');
            this.invite = document.location.href.substring(document.location.href.lastIndexOf("/") + 1);
            if (this.invite === '') {
                this.sendCreate(null);
            } else {
                this.sendOpen(this.invite);
            }
            setInterval(() => {
                let points = Board.localClient.getPoints();
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
            }, UPDATE_INTERVAL);
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
                            console.log('Add client ', new Client(dataView.getUint16(offset)));
                            offset += 2;
                            break;
                        }
                        case 1: {//remove client
                            console.log('Remove client ', clients.delete(dataView.getUint16(offset)));
                            offset += 2;
                            break;
                        }
                        case 2: {//draw
                            let client = clients.get(dataView.getUint16(offset));
                            offset += 2;
                            let size = dataView.getUint16(offset);
                            offset += 2;
                            for (let i = 0; i < size; i++) {
                                let point = {};
                                point.dt = dataView.getUint8(offset);
                                offset += 1;
                                point.x = dataView.getInt16(offset);
                                offset += 2;
                                point.y = dataView.getInt16(offset);
                                offset += 2;
                                point.usedDt = 0;
                                client.points.push(point);
                            }
                            break;
                        }
                        case 3: {//open
                            let length = dataView.getUint8(offset);
                            offset += 1;
                            let id = new TextDecoder().decode(event.data.slice(offset, offset + length));
                            offset += length;
                            length = dataView.getUint8(offset);
                            offset += 1;
                            let name = new TextDecoder().decode(event.data.slice(offset, offset + length));
                            offset += length;
                            window.history.pushState(name, document.title, '/d/' + id);
                            Board.inviteButton.innerHTML = 'invite';//todo abstract invitebutton to class???????
                            Board.sidebar.createButton(name, true, () => {
                                this.sendOpen(id);
                            });
                            break;
                        }
                        default:
                            console.error('unknown payload type ' + type + ', offset ' + offset + ', event ', event);
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

    sendOpen(id) {//todo improve this garbage javascript string encoding
        let encoded = new TextEncoder().encode(id);
        let buffer = new ArrayBuffer(2 + encoded.length);
        let dataView = new DataView(buffer);
        let offset = 0;

        dataView.setUint8(offset, 0);
        offset += 1;
        
        dataView.setUint8(offset, encoded.byteLength);
        offset += 1;

        let newBuffer = new Uint8Array(buffer);
        newBuffer.set(encoded, offset);

        this.send(newBuffer);
    }

    sendCreate(name) {
        if (name == null) {
            name = 'Untitled ' + next++;
        }
        let encoded = new TextEncoder().encode(name);
        let buffer = new ArrayBuffer(2 + encoded.length);
        let dataView = new DataView(buffer);
        let offset = 0;

        dataView.setUint8(offset, 2);
        offset += 1;
        
        dataView.setUint8(offset, encoded.byteLength);
        offset += 1;

        let newBuffer = new Uint8Array(buffer);
        newBuffer.set(encoded, offset);

        this.send(newBuffer);
    }
}