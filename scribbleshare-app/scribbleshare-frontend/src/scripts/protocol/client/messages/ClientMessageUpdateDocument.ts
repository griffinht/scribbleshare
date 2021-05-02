import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";
import ByteBuffer from "../../ByteBuffer.js";
import Document from "../../../Document.js";

export default class ClientMessageUpdateDocument extends ClientMessage {
    id: bigint;
    name: string;

    constructor(document: Document) {
        super(ClientMessageType.UPDATE_DOCUMENT);
        this.id = document.id;
        this.name = document.name;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeBigInt64(this.id);
        byteBuffer.writeString8(this.name);
    }
}