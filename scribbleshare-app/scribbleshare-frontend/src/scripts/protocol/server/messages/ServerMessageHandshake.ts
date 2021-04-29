import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer";

export default class ServerMessageHandshake extends ServerMessage {
    client: number;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.HANDSHAKE);
        this.client = byteBuffer.readInt16();
    }
}