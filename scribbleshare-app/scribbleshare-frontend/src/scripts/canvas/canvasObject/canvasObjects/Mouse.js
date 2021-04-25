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
        let object = Object.create(this.prototype);
        object.x = 0;
        object.y = 0;
        object.client = client;

        return object;
    }
}
