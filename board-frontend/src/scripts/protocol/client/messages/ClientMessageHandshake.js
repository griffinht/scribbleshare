import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageHandshake extends ClientMessage {
    constructor(invite) {
        super(ClientMessageType.HANDSHAKE);
        this.invite = invite;
    }

    getBufferSize() {
        return super.getBufferSize();
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeString(this.invite);
    }
}