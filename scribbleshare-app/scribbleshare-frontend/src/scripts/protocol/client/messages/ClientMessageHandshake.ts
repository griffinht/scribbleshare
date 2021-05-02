import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";
import ByteBuffer from "../../ByteBuffer";

export default class ClientMessageHandshake extends ClientMessage {
    invite: string;

    constructor(invite: string) {
        super(ClientMessageType.HANDSHAKE);
        this.invite = invite;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeString8(this.invite);
    }
}