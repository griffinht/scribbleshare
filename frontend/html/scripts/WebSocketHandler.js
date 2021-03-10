import BufferReader from './BufferReader.js'

class WebSocketHandler {
    constructor() {
        this.events = {};
        [
            'socket.open',
            'socket.close',
            'protocol.addClient',
            'protocol.adduser',
            'protocol.removeClient',
            'protocol.updateDocument',
            'protocol.openDocument',
            'protocol.addDocument',
            'protocol.handshake',
        ].forEach((type) => {
            this.events[type] = [];
        })

        let webSocketUrl;
        if (window.location.protocol === 'https:') {
            webSocketUrl = 'wss://localhost:443/websocket';
        } else {
            console.warn('Insecure connection, this better be a development environment');//todo disable/disallow
            webSocketUrl = 'ws://localhost:8080/websocket';
        }
        console.log('Opening WebSocket connection to ' + webSocketUrl);
        this.socket = new WebSocket(webSocketUrl);
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener('open', (event) => {
            console.log('WebSocket connection opened');
            this.dispatchEvent('socket.open');
        });

        this.socket.addEventListener('close', (event) => {
            console.log('WebSocket connection closed');
            this.dispatchEvent('socket.close');
        });
    
        this.socket.addEventListener('message', (event) => {
            if (typeof event.data === 'string') {
                console.log(event.data);
            } else {
                let reader = new BufferReader(event.data);

                while (reader.hasNext()) {
                    let type = reader.readUint8();
                    switch (type) {
                        case 0: {
                            let e = {};
                            e.id = reader.readInt16();
                            e.userId = reader.readBigInt64();
                            this.dispatchEvent('protocol.addClient', e);
                            break;
                        }
                        case 1: {
                            let e = {};
                            e.id = reader.readInt16();
                            this.dispatchEvent('protocol.removeClient', e);
                            break;
                        }
                        case 2: {
                            let e = {};
                            e.id = reader.readInt16();
                            let size = reader.readUint16();
                            e.points = [];
                            for (let i = 0; i < size; i++) {
                                let point = {};
                                point.dt = reader.readUint8();
                                point.x = reader.readInt16();
                                point.y = reader.readInt16();
                                point.usedDt = 0;
                                e.points.push(point);
                            }
                            this.dispatchEvent('protocol.updateDocument', e);
                            break;
                        }
                        case 3: {
                            let e = {};
                            e.documentId = reader.readBigInt64();
                            this.dispatchEvent('protocol.openDocument', e);
                            break;
                        }
                        case 4: {
                            let e = {};
                            e.id = reader.readBigInt64();
                            e.name = reader.readString();
                            this.dispatchEvent('protocol.addDocument', e);
                            break;
                        }
                        case 5: {
                            let e = {};
                            e.token = reader.readBigInt64();
                            e.userId = reader.readBigInt64();
                            this.dispatchEvent('protocol.handshake', e);
                            break;
                        }
                        case 6: {
                            let e = {};
                            e.user = {};
                            e.user.id = reader.readBigInt64();
                            this.dispatchEvent('protocol.adduser', e);
                            break;
                        }
                        default:
                            console.error('unknown payload type ' + type + ', offset ' + reader.position + ', event ', event);
                    }
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

    send(payload) {
        if (this.socket.readyState === WebSocket.OPEN) {
            console.log('send', payload);
            this.socket.send(payload);
        } else {
            console.error('tried to send payload while websocket was closed', payload);
        }
    }

    sendOpen(id) {
        let buffer = new ArrayBuffer(1 + 8);
        let dataView = new DataView(buffer);
        let offset = 0;

        dataView.setUint8(offset, 0);
        offset += 1;
        
        dataView.setBigInt64(offset, id);
        offset += 8;

        this.send(buffer);
    }

    sendCreate() {
        let buffer = new ArrayBuffer(1);
        let dataView = new DataView(buffer);
        let offset = 0;

        dataView.setUint8(offset, 2);
        offset += 1;

        this.send(buffer);
    }

    sendDraw(points) {
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
    }

    sendHandshake(token) {
        let buffer = new ArrayBuffer(9);
        let dataView = new DataView(buffer);
        let offset = 0;

        dataView.setUint8(offset, 3);
        offset += 1;

        console.log(token);
        dataView.setBigInt64(offset, token);
        offset += 8;

        this.send(buffer);
    }

    sendUpdateDocument(document) {

    }
}
export default new WebSocketHandler();
