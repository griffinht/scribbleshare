import ByteBuffer from "../../protocol/ByteBuffer.js";
import {Canvas} from "../Canvas.js";
import CanvasUpdateType from "./CanvasUpdateType.js";

export default abstract class CanvasUpdate {
    dt: number;

    protected constructor(byteBuffer: ByteBuffer) {
        this.dt = byteBuffer.readUint8();
    }

    draw(canvas: Canvas, dt: number): boolean {
        return true;
    }

    abstract getCanvasUpdateType(): CanvasUpdateType;

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.dt);
    }
}