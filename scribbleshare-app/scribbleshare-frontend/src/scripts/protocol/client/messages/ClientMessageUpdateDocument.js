import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageUpdateDocument extends ClientMessage {
    constructor(document) {
        super(ClientMessageType.UPDATE_DOCUMENT);
        this.id = document.id;
        this.name = document.name;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeBigInt64(this.id);
        writer.writeString8(this.name);
    }
}