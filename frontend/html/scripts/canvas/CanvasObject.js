import {ctx} from "./Canvas.js";

export default class CanvasObject {
    constructor(type, reader) {
        this.type = type;
        this.x = reader.readInt16();
        this.y = reader.readInt16();
    }

    draw(dt) {
        ctx.fillRect(this.x, this.y, 10, 10);
    }

    serialize(writer) {
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
    }
}