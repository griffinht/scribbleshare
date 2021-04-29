import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";

export default class CanvasUpdateDelete extends CanvasUpdate {
    constructor(byteBuffer: ByteBuffer) {
        super(CanvasUpdateType.DELETE);
        this.dt = byteBuffer.readUint8();
        this.id = byteBuffer.readInt16();
        this.time = 0;
    }

    draw(canvas, dt) {
        this.time += dt;
        if (this.dt <= this.time) {
            canvas.canvasObjectWrappers.delete(this.id);
            return true;
        }
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeUint8(this.dt);
        byteBuffer.writeInt16(this.id);
    }

    static create(time, id) {
        let object = Object.create(this.prototype);
        object.canvasUpdateType = CanvasUpdateType.DELETE;
        object.dt = time;
        object.id = id;
        object.time = 0;
        return object;
    }
}