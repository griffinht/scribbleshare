import CanvasObject from "../CanvasObject.js";
import {ctx} from "../Canvas.js";
import {activeDocument} from "../../Document.js";
import {apiUrl} from "../../main.js";

export default class CanvasImage extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.id = reader.readBigInt64();
        this.image = document.createElement('img');
        this.image.src = apiUrl + '/document/' + activeDocument.id + '/' + this.id;
    }

    draw() {
        ctx.drawImage(this.image, 0, 0, this.width, this.height);
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeBigInt64(this.id);
    }

    static create(x, y, id, image) {
        let shape = Object.create(this.prototype);
        shape.x = x;
        shape.y = y;
        shape.width = image.width;
        shape.height = image.height;
        shape.rotation = 0;

        shape.id = id;
        shape.image = image;
        return shape;
    }
}