import {ctx} from "./Canvas.js";

export default class CanvasObject {
    constructor(reader) {
        this.x = reader.readInt16();
        this.y = reader.readInt16();
    }

    draw() {
        ctx.fillRect(0, 0, 3, 3);
    }

    serialize(writer) {
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
    }
}