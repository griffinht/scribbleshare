import ByteBuffer from "../../protocol/ByteBuffer";
import {Canvas} from "../Canvas";

export default class CanvasUpdate {
    type: CanvasUpdateType;

    constructor(type: CanvasUpdateType) {
        this.type = type;
    }

    draw(canvas: Canvas, dt: number): boolean {
        return true;
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.type);
    }
}