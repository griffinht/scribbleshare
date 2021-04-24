export default class BufferReader {
    constructor(buffer) {
        this.position = 0;
        this.view = new DataView(buffer);
    }

    hasNext() {
        return this.position < this.view.byteLength;
    }

    readString8() {
        let length = this.readUint8();
        this.position += length;
        return new TextDecoder().decode(this.view.buffer.slice(this.position - length, this.position));
    }

    readString16() {
        let length = this.readUint16();
        this.position += length;
        return new TextDecoder().decode(this.view.buffer.slice(this.position - length, this.position));
    }

    readString32() {
        let length = this.readUint32();
        this.position += length;
        return new TextDecoder().decode(this.view.buffer.slice(this.position - length, this.position));
    }

    //todo b64 operations are probably broken
    readBase64_8() {
        return btoa(this.readString8());
    }

    readBase64_16() {
        return btoa(this.readString8());
    }

    readBase64_32() {
        return btoa(this.readString8());
    }

    readInt8() {
        let value = this.view.getInt8(this.position);
        this.position += 1;
        return value;
    }

    readUint8() {
        let value = this.view.getUint8(this.position);
        this.position += 1;
        return value;
    }

    readInt16() {
        let value = this.view.getInt16(this.position);
        this.position += 2;
        return value;
    }

    readUint16() {
        let value = this.view.getUint16(this.position);
        this.position += 2;
        return value;
    }

    readInt32() {
        let value = this.view.getInt32(this.position);
        this.position += 4;
        return value;
    }

    readUint32() {
        let value = this.view.getUint32(this.position);
        this.position += 4;
        return value;
    }

    readBigInt64() {
        let value = this.view.getBigInt64(this.position);
        this.position += 8;
        return value;
    }

    readBigUInt64() {
        let value = this.view.getBigUint64(this.position);
        this.position += 8;
        return value;
    }

    readFloat32() {
        let value = this.view.getFloat32(this.position);
        this.position += 4;
        return value;
    }

    readFloat64() {
        let value = this.view.getFloat64(this.position);
        this.position += 8;
        return value;
    }
}