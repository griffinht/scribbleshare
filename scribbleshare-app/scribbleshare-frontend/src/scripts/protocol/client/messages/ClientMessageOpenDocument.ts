import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageOpenDocument extends ClientMessage {
    constructor(document) {
        super(ClientMessageType.OPEN_DOCUMENT);
        this.id = document.id;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeBigInt64(this.id);
    }
}