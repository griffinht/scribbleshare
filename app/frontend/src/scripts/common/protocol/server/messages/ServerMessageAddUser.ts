import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageAddUser extends ServerMessage {
    user: bigint;

    constructor(byteBuffer: ByteBuffer) {
        super();
        this.user = byteBuffer.readBigInt64();
    }

    getType(): ServerMessageType {
        return ServerMessageType.ADD_USER;
    }
}