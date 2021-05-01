import ServerMessage from "../ServerMessage";
import ServerMessageType from "../ServerMessageType";
import ByteBuffer from "../../ByteBuffer";

export default class ServerMessageRemoveClient extends ServerMessage {
    id: number;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.REMOVE_CLIENT);
        this.id = byteBuffer.readInt16();
    }
}