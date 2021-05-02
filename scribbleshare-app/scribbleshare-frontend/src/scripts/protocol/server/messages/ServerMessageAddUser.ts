import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageAddUser extends ServerMessage {
    user: bigint;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.ADD_USER);
        this.user = byteBuffer.readBigInt64();
    }
}