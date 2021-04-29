import ServerMessageType from "./ServerMessageType";
import ByteBuffer from "../ByteBuffer";

export default class ServerMessage {
    type: ServerMessageType;

    constructor(type: ServerMessageType) {
        this.type = type;
    }

    serialize(byteBuffer: ByteBuffer) {

    }
}
