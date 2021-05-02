import ByteBuffer from "../../protocol/ByteBuffer.js";
import {Canvas} from "../Canvas.js";

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