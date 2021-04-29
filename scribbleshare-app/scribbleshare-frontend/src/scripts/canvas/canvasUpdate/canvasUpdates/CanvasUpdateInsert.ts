import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import CanvasObjectWrapper from "../../canvasObject/CanvasObjectWrapper.js";

export default class CanvasUpdateInsert extends CanvasUpdate {
    constructor(byteBuffer: ByteBuffer) {
        super(CanvasUpdateType.INSERT);
        this.dt = byteBuffer.readUint8();
        this.id = byteBuffer.readInt16();
        this.canvasObjectWrapper = CanvasObjectWrapper.deserialize(byteBuffer);
        this.time = 0;
    }

    draw(canvas, dt) {
        this.time += dt; //accumulated dt
        if (this.dt <= this.time) {
            canvas.canvasObjectWrappers.set(this.id, this.canvasObjectWrapper);
            return true;
        }
        return false;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeUint8(this.dt);
        byteBuffer.writeInt16(this.id);
        this.canvasObjectWrapper.serialize(byteBuffer);
    }

    static create(dt, id, canvasObjectWrapper) {
        let object = Object.create(this.prototype);
        object.canvasUpdateType = CanvasUpdateType.INSERT;
        object.dt = dt;
        object.id = id;
        object.canvasObjectWrapper = canvasObjectWrapper;
        object.time = 0;
        return object;
    }
}
