import CanvasObject from "../CanvasObject.js";
import {ctx} from "../../Canvas.js";

export default class Shape extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.type = reader.readUint8();
    }

    draw() {
        switch (this.type) {
            case Type.RECTANGLE:
                ctx.fillRect(0, 0, this.width, this.height);
                break;
            case Type.ELLIPSE:
                ctx.ellipse(0, 0, this.width, this.height, 0, 0, Math.PI * 2);
                break;
        }

    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.type);
    }

    static create(x, y, width, height) {
        let shape = Object.create(this.prototype);
        shape.dirty = true;
        shape.x = x;
        shape.y = y;
        shape.width = width;
        shape.height = height;
        shape.rotation = 0;

        shape.type = Type.RECTANGLE;
        return shape;
    }
}

const Type = {
    RECTANGLE:0,
    ELLIPSE:1,
}