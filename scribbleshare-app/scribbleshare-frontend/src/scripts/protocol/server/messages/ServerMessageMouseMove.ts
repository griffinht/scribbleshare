import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import MouseMove from "../../../MouseMove.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageMouseMove extends ServerMessage {
    client: number;
    mouseMoves: Array<MouseMove>;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.MOUSE_MOVE);
        this.client = byteBuffer.readInt16();
        this.mouseMoves = [];
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            this.mouseMoves[i] = new MouseMove(byteBuffer);
        }
    }

}