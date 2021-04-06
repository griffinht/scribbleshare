import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageHandshake extends ClientMessage {
    constructor(id, token, invite) {
        super(ClientMessageType.HANDSHAKE);
        this.id = id;
        this.token = token;
        this.invite = invite;
    }

    getBufferSize() {
        return super.getBufferSize() + 16;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeBigInt64(this.id);
        writer.writeBigInt64(this.token);
        console.log(this.invite);
        writer.writeString(this.invite);
    }
}