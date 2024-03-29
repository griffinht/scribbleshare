import {ctx} from "../../Canvas.js";
import CanvasObject from "../CanvasObject.js";
import ByteBuffer from "../../../protocol/ByteBuffer";
import CanvasObjectType from "../CanvasObjectType.js";

const pointerImage = document.createElement('img');
pointerImage.src = '/assets/pointer.png';

export default class CanvasMouse extends CanvasObject {
    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
    }

    getCanvasObjectType() {
        return CanvasObjectType.MOUSE;
    }

    draw() {
        ctx.drawImage(pointerImage, 0, 0);

    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
    }

    static create() {
        let object = Object.create(this.prototype);
        object.x = 0;
        object.y = 0;
        return object;
    }
}
