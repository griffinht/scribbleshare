import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageOpenDocument extends ClientMessage {
    constructor(id) {
        super(ClientMessageType.OPEN_DOCUMENT);
        this.id = id;
    }

    getBufferSize() {
        return super.getBufferSize() + 8;
    }

    serialize(writer) {
        super.serialize(writer);

        writer.writeBigInt64(this.id);
    }
}