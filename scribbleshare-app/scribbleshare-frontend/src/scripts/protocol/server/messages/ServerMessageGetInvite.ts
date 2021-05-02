import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageGetInvite extends ServerMessage {
    code: string;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.GET_INVITE);
        this.code = byteBuffer.readString8();
    }
}