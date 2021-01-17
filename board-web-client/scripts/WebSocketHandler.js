class WebSocketHandler {
    constructor() {
        this.events = {};
        [
            'socketopen',
            'socketclose',
            'addclient',
            'removeclient',
            'draw',
            'open',
        ].forEach((type) => {
            this.events[type] = [];
        })

        this.socket = new WebSocket('ws://localhost/websocket');
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener('open', (event) => {
            console.log('WebSocket connection opened');
            this.dispatchEvent('socketopen');
        });

        this.socket.addEventListener('close', (event) => {
            console.log('WebSocket connection closed');
            this.dispatchEvent('socketclose');
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
                            let e = {};
                            e.id = dataView.getUint16(offset);
                            offset += 2;
                            this.dispatchEvent('addclient', e);
                            break;
                        }
                        case 1: {//remove client
                            let e = {};
                            e.id = dataView.getUint16(offset);
                            offset += 2;
                            this.dispatchEvent('removeclient', e);
                            break;
                        }
                        case 2: {//draw
                            let e = {};
                            e.id = dataView.getUint16(offset);
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
                                e.points.push(point);
                            }
                            this.dispatchEvent('draw', e);
                            break;
                        }
                        case 3: {//open
                            let e = {};
                            let length = dataView.getUint8(offset);
                            offset += 1;
                            e.id = new TextDecoder().decode(event.data.slice(offset, offset + length));
                            offset += length;
                            length = dataView.getUint8(offset);
                            offset += 1;
                            e.name = new TextDecoder().decode(event.data.slice(offset, offset + length));
                            offset += length;
                            this.dispatchEvent('open', e);
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
}
export default new WebSocketHandler();
