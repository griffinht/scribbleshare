import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageHandshake extends ServerMessage {
    client: number;

    constructor(byteBuffer: ByteBuffer) {
        super();
        this.client = byteBuffer.readInt16();
    }

    getType(): ServerMessageType {
        return ServerMessageType.HANDSHAKE;
    }
}