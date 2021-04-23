import {ctx} from "../Canvas.js";

export default class CanvasObject {
    constructor(reader) {
        this.x = reader.readInt16();
        this.y = reader.readInt16();
        this.width = reader.readUint16();
        this.height = reader.readUint16();
        this.rotation = reader.readUint8();
    }

    lerp(original, target, t) {
        this.x = lerp(original.x, target.x, t);
        this.y = lerp(original.y, target.y, t);
    }

    draw() {
        ctx.fillRect(0, 0, this.width, this.height);
    }

    serialize(writer) {
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
        writer.writeUint16(this.width);
        writer.writeUint16(this.height);
        writer.writeUint8(this.rotation);
    }

    static create(canvasObject) {
        let object = Object.create(this.prototype);
        object.x = canvasObject.x;
        object.y = canvasObject.y;
        object.width = canvasObject.width;
        object.height = canvasObject.height;
        object.rotation = canvasObject.rotation;
        return object;
    }
}

function lerp(v0, v1, t) {
    return v0 * (1 - t) + v1 * t;
}