import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageDeleteDocument extends ServerMessage {
    id: bigint;

    constructor(byteBuffer: ByteBuffer) {
        super();
        this.id = byteBuffer.readBigInt64();
    }

    getType(): ServerMessageType {
        return ServerMessageType.DELETE_DOCUMENT;
    }
}