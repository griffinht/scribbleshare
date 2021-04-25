import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";

export default class CanvasUpdateDelete extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.DELETE);
        this.dt = reader.readUint8();
        this.id = reader.readInt16();
        this.time = 0;
    }

    isDirty() {
        return this.id !== null;
    }

    draw(canvas, dt) {
        if (this.dt === null) {
            return;
        }
        this.time += dt;
        if (this.dt <= this.time) {
            canvas.canvasObjectWrappers.delete(this.id);
            this.id = null;
        }
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.dt);
        writer.writeInt16(this.id);
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