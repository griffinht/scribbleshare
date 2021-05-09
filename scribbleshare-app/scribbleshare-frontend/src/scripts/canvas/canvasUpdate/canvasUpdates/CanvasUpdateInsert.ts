import CanvasUpdate from "../CanvasUpdate.js";
import CanvasObjectWrapper from "../../canvasObject/CanvasObjectWrapper.js";
import ByteBuffer from "../../../protocol/ByteBuffer.js";
import {Canvas} from "../../Canvas.js";
import CanvasUpdateType from "../CanvasUpdateType.js";

export default class CanvasUpdateInsert extends CanvasUpdate {
    dt: number;
    id: number;
    canvasObjectWrapper: CanvasObjectWrapper;
    time: number;

    constructor(byteBuffer: ByteBuffer) {
        super(CanvasUpdateType.INSERT);
        this.dt = byteBuffer.readUint8();
        this.id = byteBuffer.readInt16();
        this.canvasObjectWrapper = CanvasObjectWrapper.deserialize(byteBuffer);
        this.time = 0;
    }

    draw(canvas: Canvas, dt: number) {
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

    static create(dt: number, id: number, canvasObjectWrapper: CanvasObjectWrapper) {
        let object: CanvasUpdateInsert = Object.create(this.prototype);
        object.type = CanvasUpdateType.INSERT;
        object.dt = dt;
        object.id = id;
        object.canvasObjectWrapper = canvasObjectWrapper;
        object.time = 0;
        return object;
    }
}
