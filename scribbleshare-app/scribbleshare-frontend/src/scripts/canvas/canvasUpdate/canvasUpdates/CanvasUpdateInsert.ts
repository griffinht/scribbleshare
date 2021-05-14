import CanvasUpdate from "../CanvasUpdate.js";
import ByteBuffer from "../../../protocol/ByteBuffer.js";
import {Canvas} from "../../Canvas.js";
import CanvasUpdateType from "../CanvasUpdateType.js";
import CanvasObject from "../../canvasObject/CanvasObject.js";
import {getCanvasObject} from "../../canvasObject/CanvasObjectType.js";

export default class CanvasUpdateInsert extends CanvasUpdate {
    canvasObject: CanvasObject;

    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.canvasObject = getCanvasObject(byteBuffer.readUint8(), byteBuffer);
    }

    getCanvasUpdateType(): CanvasUpdateType {
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
        byteBuffer.writeUint8(this.canvasObject.getCanvasObjectType());
        this.canvasObject.serialize(byteBuffer);
    }

    static create(dt: number, id: number, canvasObject: CanvasObject) {
        let object: CanvasUpdateInsert = Object.create(this.prototype);//todo this on everything is
        object.dt = dt;
        object.canvasObject = canvasObject;
        return object;
    }
}
