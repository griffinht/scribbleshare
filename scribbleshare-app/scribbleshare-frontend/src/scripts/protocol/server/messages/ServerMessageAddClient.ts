import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer";

export default class ServerMessageAddClient extends ServerMessage {
    clientUserTuples: Array<[number, bigint]>;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.ADD_CLIENT);
        this.clientUserTuples = [];
        let length = byteBuffer.readUint16();
        for (let i = 0; i < length; i++) {
            this.clientUserTuples[i] = [byteBuffer.readInt16(), byteBuffer.readBigInt64()];
        }
    }
}