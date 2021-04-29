import {ctx} from "../../Canvas.js";
import {activeDocument} from "../../../Document.js";
import {apiUrl} from "../../../main.js";
import EntityCanvasObject from "../EntityCanvasObject.js";

export default class CanvasImage extends EntityCanvasObject {
    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.id = byteBuffer.readBigInt64();
        this.image = document.createElement('img');
        this.image.src = apiUrl + '/document/' + activeDocument.id + '/' + this.id;
    }

    draw() {
        ctx.drawImage(this.image, 0, 0, this.width, this.height);
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeBigInt64(this.id);
    }

    static create(x, y, id, image) {
        let object = Object.create(this.prototype);
        object.x = x;
        object.y = y;
        object.width = image.width;
        object.height = image.height;
        object.rotation = 0;
        object.id = id;
        object.image = image;
        return object;
    }
}