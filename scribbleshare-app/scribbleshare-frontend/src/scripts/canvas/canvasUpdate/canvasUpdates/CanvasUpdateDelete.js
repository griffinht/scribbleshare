import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";

export default class CanvasUpdateDelete extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.DELETE);
        this.canvasDeletes = [];
        this.time = 0;
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
        this.time = 0;
    }

    delete(time, id) {
        this.canvasDeletes.push(CanvasDelete.create(time - this.time, id));
        this.time = time;//last time
    }

    draw(canvas, dt) {
        this.time += dt;
        while(this.canvasDeletes.length > 0) {
            if (this.canvasDeletes[0].dt <= this.time) {
                this.time -= this.canvasDeletes[0].dt;//should be near 0
                canvas.canvasObjectWrappers.delete(this.canvasDeletes[0].id);
                this.canvasDeletes.shift();
            } else {
                break;
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
        object.time = 0;
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