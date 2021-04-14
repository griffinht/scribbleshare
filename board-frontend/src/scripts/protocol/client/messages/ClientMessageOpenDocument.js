import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageOpenDocument extends ClientMessage {
    constructor(document) {
        super(ClientMessageType.OPEN_DOCUMENT);
        this.id = document.id;
    }

    getBufferSize() {
        return super.getBufferSize() + 8;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeBigInt64(this.id);
    }
}