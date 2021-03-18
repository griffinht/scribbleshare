import CanvasObject from "../CanvasObject.js";
import {CanvasObjectType} from "../CanvasObjectType.js";

export default class ShapeObject extends CanvasObject {
    constructor(x, y, length, width) {
        super(CanvasObjectType.SHAPE);
        this.x = x;
        this.y = y;
        this.length = length;
        this.width = width;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
        writer.writeInt16(this.length);
        writer.writeInt16(this.width);
    }
}