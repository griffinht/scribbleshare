import {ctx} from "./Canvas.js";

export default class CanvasObject {
    constructor(reader) {
        this.dirty = false;
        this.x = reader.readInt16();
        this.y = reader.readInt16();
        this.width = reader.readUint16();
        this.height = reader.readUint16();
        this.rotation = reader.readUint8();
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
}