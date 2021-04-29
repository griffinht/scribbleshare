export default class ClientMessage {
    constructor(type) {
        this.type = type;
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.type);
    }
}