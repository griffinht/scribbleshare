import CanvasObject from "../CanvasObject.js";
import {ctx} from "../Canvas.js";

export default class Shape extends CanvasObject {
    constructor(reader) {
        super(reader);
    }

    draw() {
        ctx.fillRect(0, 0, this.width, this.height);
    }

    serialize(writer) {
        super.serialize(writer);
    }

    static create(x, y, width, height) {
        let shape = Object.create(this.prototype);
        shape.x = x;
        shape.y = y;
        shape.width = width;
        shape.height = height;
        shape.rotation = 0;
        return shape;
    }
}