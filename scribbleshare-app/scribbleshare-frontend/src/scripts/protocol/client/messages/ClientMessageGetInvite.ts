import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ClientMessageGetInvite extends ClientMessage {
    getType(): ClientMessageType {
        return ClientMessageType.GET_INVITE;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
    }
}