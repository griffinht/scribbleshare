export default class BufferWriter {
    constructor(buffer) {
        this.position = 0;
        this.view = new DataView(buffer);
    }

    writeString(value) {//todo improve performance
        this.writeUint8(value);
        return new TextDecoder().decode(this.view.buffer.slice(this.position, this.position + length));
    }

    writeInt8(value) {
        this.view.setInt8(this.position, value);
        this.position += 1;
    }

    writeUint8(value) {
        this.view.setUint8(this.position, value);
        this.position += 1;
    }

    writeInt16(value) {
        this.view.setInt16(this.position, value);
        this.position += 2;
    }

    writeUint16(value) {
        this.view.setUint16(this.position, value);
        this.position += 2;
    }

    writeInt32(value) {
        this.view.setInt32(this.position, value);
        this.position += 4;
    }

    writeUint32(value) {
        this.view.setUint32(this.position, value);
        this.position += 4;
    }

    writeBigInt64(value) {
        this.view.setBigInt64(this.position, value);
        this.position += 8;
    }

    writeBigUInt64(value) {
        this.view.setBigUint64(this.position, value);
        this.position += 8;
    }

    writeFloat32(value) {
        this.view.setFloat32(this.position, value);
        this.position += 4;
    }

    writeFloat64(value) {
        this.view.setFloat64(this.position, value);
        this.position += 8;
    }
}