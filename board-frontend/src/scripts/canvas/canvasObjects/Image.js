import CanvasObject from "../CanvasObject.js";
import {ctx} from "../Canvas.js";

export default class Image extends CanvasObject {
    constructor(reader) {
        super(reader);
        let size = reader.readUint32();

    }

    draw() {
        ctx.drawImage(this.image, this.x, this.y);
    }

    serialize(writer) {
        super.serialize(writer);

    }

    static create(x, y, image) {
        let shape = Object.create(this.prototype);
        shape.x = x;
        shape.y = y;
        shape.image = image;
        return shape;
    }
}