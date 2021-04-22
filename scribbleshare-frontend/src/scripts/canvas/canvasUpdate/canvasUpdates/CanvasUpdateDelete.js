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

    isDirty() {
        return this.canvasDeletes.length > 0;
    }

    clear() {
        this.canvasDeletes.length = 0;
    }

    delete(dt, id) {
        this.canvasDeletes.push(CanvasDelete.create(dt, id));
    }

    draw(canvas, dt) {
        for (let i = 0; i < this.canvasDeletes.length; i++) {
            if (this.canvasDeletes[i].dt >= dt) {
                canvas.canvasObjectWrappers.delete(this.canvasDeletes[i].id);
                this.canvasDeletes.splice(i--, 1);
            }
        }
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasDeletes.length);
        this.canvasDeletes.forEach((canvasDelete) => {
            canvasDelete.serialize(writer);
        });
    }

    static create() {
        let object = Object.create(this.prototype);
        object.canvasUpdateType = CanvasUpdateType.DELETE;
        object.canvasDeletes = [];
        return object;
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