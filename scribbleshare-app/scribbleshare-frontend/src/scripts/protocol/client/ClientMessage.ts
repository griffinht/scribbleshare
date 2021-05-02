import ClientMessageType from "./ClientMessageType.js";
import ByteBuffer from "../ByteBuffer.js";

export default class ClientMessage {
    type:ClientMessageType;

    constructor(type: ClientMessageType) {
        this.type = type;
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.type);
    }
}