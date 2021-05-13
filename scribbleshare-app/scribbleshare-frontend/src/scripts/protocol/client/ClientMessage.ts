import ClientMessageType from "./ClientMessageType.js";
import ByteBuffer from "../ByteBuffer.js";

export default abstract class ClientMessage {
    abstract getType(): ClientMessageType;

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.getType());
    }
}