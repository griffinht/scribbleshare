import {ctx} from "../../Canvas.js";
import CanvasObject from "../CanvasObject.js";
const pointerImage = document.createElement('img');
pointerImage.src = '/assets/pointer.png';

export default class Mouse extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.client = reader.readInt16();
    }

    draw() {
        ctx.drawImage(pointerImage, 0, 0);

    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeInt16(this.client);
    }

    static create(client) {
        let shape = Object.create(this.prototype);
        shape.dirty = true;
        shape.x = 0;
        shape.y = 0;
        shape.width = 0;
        shape.height = 0;
        shape.rotation = 0;
        shape.client = client;

        return shape;
    }
}
