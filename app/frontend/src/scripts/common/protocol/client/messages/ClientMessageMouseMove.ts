import ClientMessage from "../ClientMessage.js";
import ClientMessageType from "../ClientMessageType.js";
import MouseMove from "../../../MouseMove.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ClientMessageMouseMove extends ClientMessage {
    mouseMoves: Array<MouseMove>;

    constructor(mouseMoves: Array<MouseMove>) {
        super();
        this.mouseMoves = mouseMoves;
    }

    getType(): ClientMessageType {
        return ClientMessageType.MOUSE_MOVE;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeUint8(this.mouseMoves.length);
        for (let i = 0; i < this.mouseMoves.length; i++) {
            this.mouseMoves[i].serialize(byteBuffer);
        }
    }
}