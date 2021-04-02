import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageHandshake extends ClientMessage {
    constructor(token) {
        super(ClientMessageType.HANDSHAKE);
        this.token = token;
    }

    getBufferSize() {
        return super.getBufferSize() + 8;
    }

    serialize(writer) {
        super.serialize(writer);

        writer.writeBigInt64(this.token)
    }
}