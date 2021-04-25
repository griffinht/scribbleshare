import {ctx} from "../../Canvas.js";
import EntityCanvasObject from "../EntityCanvasObject.js";

export default class Shape extends EntityCanvasObject {
    constructor(reader) {
        super(reader);
        this.type = reader.readUint8();
    }

    draw() {
        switch (this.type) {
            case ShapeType.RECTANGLE:
                ctx.fillRect(this.x, this.y, this.width, this.height);
                break;
            case ShapeType.ELLIPSE:
                ctx.ellipse(this.x, this.y, this.width, this.height, 0, 0, Math.PI * 2);
                break;
        }

    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.type);
    }

    static create(x, y, width, height, type) {
        let object = Object.create(this.prototype);
        object.x = x;
        object.y = y;
        object.width = width;
        object.height = height;
        object.rotation = 0;
        object.type = type;
        return object;
    }
}

export const ShapeType = {
    RECTANGLE:0,
    ELLIPSE:1,
}