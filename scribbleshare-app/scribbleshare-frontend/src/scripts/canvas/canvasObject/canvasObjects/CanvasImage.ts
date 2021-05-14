import {ctx} from "../../Canvas.js";
import {activeDocument} from "../../../Document.js";
import EntityCanvasObject from "../EntityCanvasObject.js";
import ByteBuffer from "../../../protocol/ByteBuffer.js";
import Environment from "../../../Environment.js";
import CanvasObjectType from "../CanvasObjectType.js";

export default class CanvasImage extends EntityCanvasObject {
    id: bigint;
    image: HTMLImageElement;

    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.id = byteBuffer.readBigInt64();
        this.image = document.createElement('img');
        // @ts-ignore todo
        this.image.src = Environment.API_HOST + '/document/' + activeDocument.id + '/' + this.id;
    }

    getCanvasObjectType() {
        return CanvasObjectType.IMAGE;
    }

    draw() {
        ctx.drawImage(this.image, 0, 0, this.width, this.height);
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeBigInt64(this.id);
    }

    static create(x: number, y: number, id: bigint, image: HTMLImageElement) {
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