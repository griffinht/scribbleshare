import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";
import Document from "../../../Document.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ClientMessageDeleteDocument extends ClientMessage {
    id: bigint;

    constructor(document: Document) {
        super(ClientMessageType.DELETE_DOCUMENT);
        this.id = document.id;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeBigInt64(this.id);
    }
}