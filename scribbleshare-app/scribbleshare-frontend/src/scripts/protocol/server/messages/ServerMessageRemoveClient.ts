import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageRemoveClient extends ServerMessage {
    id: number;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.REMOVE_CLIENT);
        this.id = byteBuffer.readInt16();
    }
}