import CanvasObject from "../CanvasObject.js";
import {ctx} from "../Canvas.js";

export default class Shape extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.width = reader.readUint16();
        this.height = reader.readUint16();
    }

    draw() {
        ctx.fillRect(this.x, this.y, this.width, this.height);
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeInt16(this.width);
        writer.writeInt16(this.height);
    }

    static create(x, y, width, height) {
        let shape = Object.create(this.prototype);
        shape.x = x;
        shape.y = y;
        shape.width = width;
        shape.height = height;
        return shape;
    }
}