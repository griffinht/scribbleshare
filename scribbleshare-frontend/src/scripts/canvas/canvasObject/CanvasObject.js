import {ctx} from "../Canvas.js";

export default class CanvasObject {
    constructor(reader) {
        this.x = reader.readInt16();
        this.y = reader.readInt16();
        this.width = reader.readUint16();
        this.height = reader.readUint16();
        this.rotation = reader.readUint8();
        this.original = CanvasObject.create(this);
        this.dt = 0;
    }

    lerp(canvasObject, t) {
        this.x = lerp(this.original.x, canvasObject.x, t);
        this.y = lerp(this.original.y, canvasObject.y, t);
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