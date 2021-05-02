import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer";

export default class ServerMessageDeleteDocument extends ServerMessage {
    id: bigint;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.DELETE_DOCUMENT);
        this.id = byteBuffer.readBigInt64();
    }
}