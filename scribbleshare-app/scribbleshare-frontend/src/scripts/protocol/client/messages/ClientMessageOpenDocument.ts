import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";
import ByteBuffer from "../../ByteBuffer.js";
import Document from "../../../Document.js";

export default class ClientMessageOpenDocument extends ClientMessage {
    id: bigint;

    constructor(document: Document) {
        super(ClientMessageType.OPEN_DOCUMENT);
        this.id = document.id;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeBigInt64(this.id);
    }
}