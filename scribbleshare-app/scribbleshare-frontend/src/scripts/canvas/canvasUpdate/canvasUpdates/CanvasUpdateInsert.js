import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import CanvasObjectWrapper from "../../canvasObject/CanvasObjectWrapper.js";

export default class CanvasUpdateInsert extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.INSERT);
        this.dt = reader.readUint8();
        this.id = reader.readInt16();
        this.canvasObjectWrapper = CanvasObjectWrapper.deserialize(reader);
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

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.dt);
        writer.writeInt16(this.id);
        this.canvasObjectWrapper.serialize(writer);
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
