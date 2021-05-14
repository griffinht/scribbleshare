import CanvasUpdate from "../CanvasUpdate.js";
import CanvasObjectWrapper from "../../canvasObject/CanvasObjectWrapper.js";
import ByteBuffer from "../../../protocol/ByteBuffer.js";
import {Canvas} from "../../Canvas.js";
import CanvasUpdateType from "../CanvasUpdateType.js";

export default class CanvasUpdateInsert extends CanvasUpdate {
    canvasObjectWrapper: CanvasObjectWrapper;

    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.canvasObjectWrapper = CanvasObjectWrapper.deserialize(byteBuffer);
    }

    getType(): CanvasUpdateType {
        return CanvasUpdateType.INSERT;
    }

    draw(canvas: Canvas, dt: number) {
/*        this.time += dt; //accumulated dt
        if (this.dt <= this.time) {
            canvas.canvasObjectWrappers.set(this.id, this.canvasObjectWrapper);
            return true;
        }*/
        return false;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        this.canvasObjectWrapper.serialize(byteBuffer);
    }

    static create(dt: number, id: number, canvasObjectWrapper: CanvasObjectWrapper) {
        let object: CanvasUpdateInsert = Object.create(this.prototype);//todo this on everything is
        object.dt = dt;
        object.canvasObjectWrapper = canvasObjectWrapper;
        return object;
    }
}
