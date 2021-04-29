import ServerMessage from "../ServerMessage";
import ServerMessageType from "../ServerMessageType";
import ByteBuffer from "../../ByteBuffer";

export default class ServerMessageUpdateDocument extends ServerMessage {
    shared: boolean;
    id: bigint;
    name: string;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.UPDATE_DOCUMENT);
        this.shared = byteBuffer.readUint8() == 1;//todo broken last time i checked
        this.id = byteBuffer.readBigInt64();
        this.name = byteBuffer.readString8();
    }
}