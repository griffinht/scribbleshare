import ServerMessage from "../ServerMessage.js";
import ByteBuffer from "../../ByteBuffer.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageUpdateDocument extends ServerMessage {
    shared: boolean;
    id: bigint;
    name: string;

    constructor(byteBuffer: ByteBuffer) {
        super();
        this.shared = byteBuffer.readUint8() == 1;//todo broken last time i checked
        this.id = byteBuffer.readBigInt64();
        this.name = byteBuffer.readString8();
    }

    getType(): ServerMessageType {
        return ServerMessageType.UPDATE_DOCUMENT;
    }
}