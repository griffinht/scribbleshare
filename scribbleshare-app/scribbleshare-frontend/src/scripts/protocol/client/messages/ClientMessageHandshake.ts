import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageHandshake extends ClientMessage {
    constructor(invite) {
        super(ClientMessageType.HANDSHAKE);
        this.invite = invite;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeString8(this.invite);
    }
}