import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageDeleteDocument extends ClientMessage {
    constructor(document) {
        super(ClientMessageType.DELETE_DOCUMENT);
        this.id = document.id;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeBigInt64(this.id);
    }
}