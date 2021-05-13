import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageRemoveClient extends ServerMessage {
    id: number;

    constructor(byteBuffer: ByteBuffer) {
        super();
        this.id = byteBuffer.readInt16();
    }

    getType(): ServerMessageType {
        return ServerMessageType.REMOVE_CLIENT;
    }
}