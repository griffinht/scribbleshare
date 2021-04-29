import ClientMessageType from "./ClientMessageType";
import ByteBuffer from "../ByteBuffer";

export default class ClientMessage {
    type:ClientMessageType;

    constructor(type: ClientMessageType) {
        this.type = type;
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.type);
    }
}