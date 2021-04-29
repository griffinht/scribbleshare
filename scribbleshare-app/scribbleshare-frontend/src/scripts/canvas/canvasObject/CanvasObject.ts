import {ctx, lerp} from "../Canvas.js";

export default class CanvasObject {
    constructor(reader) {
        this.x = reader.readInt16();
        this.y = reader.readInt16();
        this.original = null;
    }

    lerp(target, t) {
        this.x = lerp(this.original.x, target.x, t);
        this.y = lerp(this.original.y, target.y, t);
    }

    draw() {
        ctx.fillRect(0, 0, 10, 10);
    }

    serialize(writer) {
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
    }

    static clone(canvasObject) {
        let object = Object.create(this.prototype);
        object.x = canvasObject.x;
        object.y = canvasObject.y;
        return object;
    }
}
