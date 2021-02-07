class WebSocketHandler {
    constructor() {
        this.events = {};
        [
            'socket.open',
            'socket.close',
            'protocol.addclient',
            'protocol.removeclient',
            'protocol.draw',
            'protocol.opendocument',
            'protocol.adddocument',
            'protocol.handshake',
        ].forEach((type) => {
            this.events[type] = [];
        })

        let webSocketUrl;
        if (window.location.protocol === 'https:') {
            webSocketUrl = 'wss://localhost/websocket';
        } else {
            webSocketUrl = 'ws://localhost/websocket';
        }
        this.socket = new WebSocket(webSocketUrl);
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener('open', (event) => {
            console.log('WebSocket connection to ' + webSocketUrl + ' opened');
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
                let dataView = new DataView(event.data);
                let offset = 0;

                while (offset < dataView.byteLength) {
                    let type = dataView.getUint8(offset);
                    offset += 1;
                    console.log('got ' + type);
                    switch (type) {
                        case 0: {
                            let e = {};
                            e.id = dataView.getBigInt64(offset);
                            offset += 8;
                            this.dispatchEvent('protocol.addclient', e);
                            break;
                        }
                        case 1: {
                            let e = {};
                            e.id = dataView.getBigInt64(offset);
                            offset += 8;
                            this.dispatchEvent('protocol.removeclient', e);
                            break;
                        }
                        case 2: {
                            let e = {};
                            e.id = dataView.getBigInt64(offset);
                            offset += 8;
                            let size = dataView.getUint16(offset);
                            offset += 2;
                            e.points = [];
                            for (let i = 0; i < size; i++) {
                                let point = {};
                                point.dt = dataView.getUint8(offset);
                                offset += 1;
                                point.x = dataView.getInt16(offset);
                                offset += 2;
                                point.y = dataView.getInt16(offset);
                                offset += 2;
                                point.usedDt = 0;
                                e.points.push(point);
                            }
                            this.dispatchEvent('protocol.draw', e);
                            break;
                        }
                        case 3: {
                            let e = {};
                            e.id = dataView.getBigInt64(offset);
                            offset += 8;
                            this.dispatchEvent('protocol.opendocument', e);
                            break;
                        }
                        case 4: {
                            let e = {};
                            e.id = dataView.getBigInt64(offset);
                            offset += 8;
                            length = dataView.getUint8(offset);
                            offset += 1;
                            e.name = new TextDecoder().decode(event.data.slice(offset, offset + length));
                            offset += length;
                            this.dispatchEvent('protocol.adddocument', e);
                            break;
                        }
                        case 5: {
                            let e = {};
                            e.token = dataView.getBigInt64(offset);
                            offset += 8;
                            this.dispatchEvent('protocol.handshake', e);
                            break;
                        }
                        default:
                            console.error('unknown payload type ' + type + ', offset ' + offset + ', event ', event);
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
        console.log(type, event);
        this.events[type].forEach(onevent => onevent(event));
    }

    send(payload) {
        if (this.socket.readyState === WebSocket.OPEN) {
            console.log('sending');
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
        offset += 1;

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
}
export default new WebSocketHandler();
