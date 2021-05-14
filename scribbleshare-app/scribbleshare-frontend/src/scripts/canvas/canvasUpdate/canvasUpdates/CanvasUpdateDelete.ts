import CanvasUpdate from "../CanvasUpdate.js";
import ByteBuffer from "../../../protocol/ByteBuffer.js";
import {Canvas} from "../../Canvas.js";
import CanvasUpdateType from "../CanvasUpdateType.js";

export default class CanvasUpdateDelete extends CanvasUpdate {
    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
    }

    getType(): CanvasUpdateType {
        return CanvasUpdateType.DELETE;
    }

    draw(canvas: Canvas, dt: number): boolean {
        /*        this.time += dt;
                if (this.dt <= this.time) {
                    canvas.canvasObjectWrappers.delete(this.id);
                    return true;
                }*/
        return false;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
    }

    static create(time: number, id: number) {
        let object: CanvasUpdateDelete = Object.create(this.prototype);
        object.dt = time;
        return object;
    }
}