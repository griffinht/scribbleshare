import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";

export default class CanvasUpdateDelete extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.DELETE);
        this.canvasDeletes = [];
        this.lastDelete = 0;
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
        let canvasDelete = CanvasDelete.create(dt, id);
        if (this.canvasDeletes.length > 0) {
            canvasDelete.dt -= this.lastDelete;
        }
        this.lastDelete = dt;
        this.canvasDeletes.push(canvasDelete);
    }

    draw(canvas, dt) {
        while(this.canvasDeletes.length > 0) {
            if (this.canvasDeletes[0].dt <= dt) {
                canvas.canvasObjectWrappers.delete(this.canvasDeletes[0].id);
                if (this.canvasDeletes.length >= 2) {
                    this.canvasDeletes[1].dt += this.canvasDeletes[0].dt;
                }
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