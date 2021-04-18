import CanvasObject from "../CanvasObject.js";
import {ctx} from "../Canvas.js";
import {activeDocument} from "../../Document.js";

export default class CanvasImage extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.image = document.createElement('img');
        this.image.src = apiUrl + '/resource/' + activeDocument.id + '/' + reader.readBigInt64();
    }

    draw() {
        ctx.drawImage(this.image, this.x, this.y);
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.type);
    }

    static create(x, y, image) {
        let shape = Object.create(this.prototype);
        shape.x = x;
        shape.y = y;
        shape.image = image;
        return shape;
    }
}