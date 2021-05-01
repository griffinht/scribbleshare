import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";
import ByteBuffer from "../../ByteBuffer";

export default class ClientMessageGetInvite extends ClientMessage {
    constructor() {
        super(ClientMessageType.GET_INVITE);
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
    }
}