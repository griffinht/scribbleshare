import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";
import Document from "../../../Document.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ClientMessageDeleteDocument extends ClientMessage {
    id: bigint;

    constructor(document: Document) {
        super();
        this.id = document.id;
    }

    getType(): ClientMessageType {
        return ClientMessageType.DELETE_DOCUMENT;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeBigInt64(this.id);
    }
}