export default class BufferWriter {//todo rename to buffered buffer writer?
    constructor(size) {
        if (size == null) {
            size = 0;
        }
        this.position = 0;
        this.view = new DataView(new ArrayBuffer(size));
    }

    writeString(value) {//todo improve performance
        this.writeUint8(value.length);
        let buffer = new TextEncoder().encode(value);
        this.checkResize(buffer.length);
        copy(buffer, this.view.buffer, this.position);
        this.position += buffer.length;
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
            copy(this.view.buffer, buffer)
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

function copy(sourceBuffer, destinationBuffer, offset) {
    let sourceArray = new Uint8Array(sourceBuffer);
    let destinationArray = new Uint8Array(destinationBuffer);
    destinationArray.set(sourceArray, offset);
}