import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";

export default class CanvasUpdateDelete extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.DELETE);
        this.canvasDeletes = [];
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasDeletes.push(new CanvasDelete(reader));
        }
    }

    update(canvas) {
        this.canvasDeletes.forEach((canvasDelete) => {
            canvas.canvasDeletes.push(canvasDelete);
        });
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasDeletes.length);
        this.canvasDeletes.forEach((canvasDelete) => {
            canvasDelete.serialize(writer);
        });
    }

    static create(canvasDeletes) {
        let object = Object.create(this.prototype);
        object.canvasDeletes = canvasDeletes;
        return canvasDeletes;
    }
}

class CanvasDelete {
    constructor(reader) {
        this.dt = reader.readUint8();
        this.id = reader.readInt16();
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.id);
    }

    static create(dt, id) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.id = id;
        return object;
    }
}