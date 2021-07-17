import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ClientMessageHandshake extends ClientMessage {
    invite: string;

    constructor(invite: string) {
        super();
        this.invite = invite;
    }

    getType(): ClientMessageType {
        return ClientMessageType.HANDSHAKE;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeString8(this.invite);
    }
}