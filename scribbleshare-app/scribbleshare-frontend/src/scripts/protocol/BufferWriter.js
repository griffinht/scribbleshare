export default class BufferWriter {//auto resize buffer writer todo improve efficiency/speed of resizes todo merge with BufferReader
    constructor(size) {
        if (size == null) {
            size = 0;
        }
        this.position = 0;
        this.view = new DataView(new ArrayBuffer(size));
    }

    writeString8(value) {
        this.writeUint8(value.length);
        let buffer = new TextEncoder().encode(value);
        this.checkResize(buffer.length);
        new Uint8Array(this.view.buffer).set(new Uint8Array(buffer), this.position);
        this.position += buffer.length;
    }

    writeString16(value) {
        this.writeUint16(value.length);
        let buffer = new TextEncoder().encode(value);
        this.checkResize(buffer.length);
        new Uint8Array(this.view.buffer).set(new Uint8Array(buffer), this.position);
        this.position += buffer.length;
    }

    writeString32(value) {
        this.writeUint32(value.length);
        let buffer = new TextEncoder().encode(value);
        this.checkResize(buffer.length);
        new Uint8Array(this.view.buffer).set(new Uint8Array(buffer), this.position);
        this.position += buffer.length;
    }

    writeBase64_8(value) {
        this.writeString8(atob(value));
    }

    writeBase64_16(value) {
        this.writeString16(atob(value));
    }

    writeBase64_32(value) {
        this.writeString32(atob(value));
    }

    writeInt8(value) {
        this.checkResize(1);
        this.view.setInt8(this.position, value);
        this.position += 1;
    }

    writeUint8(value) {
        this.checkResize(1);
        this.view.setUint8(this.position, value);
        this.position += 1;
    }

    writeInt16(value) {
        this.checkResize(2);
        this.view.setInt16(this.position, value);
        this.position += 2;
    }

    writeUint16(value) {
        this.checkResize(2);
        this.view.setUint16(this.position, value);
        this.position += 2;
    }

    writeInt32(value) {
        this.checkResize(4);
        this.view.setInt32(this.position, value);
        this.position += 4;
    }

    writeUint32(value) {
        this.checkResize(4);
        this.view.setUint32(this.position, value);
        this.position += 4;
    }

    writeBigInt64(value) {
        this.checkResize(8);
        this.view.setBigInt64(this.position, value);
        this.position += 8;
    }

    writeBigUInt64(value) {
        this.checkResize(8);
        this.view.setBigUint64(this.position, value);
        this.position += 8;
    }

    writeFloat32(value) {
        this.checkResize(4);
        this.view.setFloat32(this.position, value);
        this.position += 4;
    }

    writeFloat64(value) {
        this.checkResize(1);
        this.view.setFloat64(this.position, value);
        this.position += 8;
    }

    checkResize(extraLength) {//todo not very efficient
        let targetLength = this.position + extraLength;
        if (targetLength > this.view.byteLength) {
            let buffer = new ArrayBuffer(targetLength);
            new Uint8Array(buffer).set(new Uint8Array(this.view.buffer));
            this.view = new DataView(buffer);
        }
    }

    getBuffer() {
        if (this.view.byteLength > this.position) {
            return this.view.buffer.slice(0, this.position);
        } else {
            return this.view.buffer;
        }
    }
}
