import CanvasObject from "../CanvasObject.js";
import {ctx} from "../Canvas.js";

export default class CanvasImage extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.type = reader.readUint8();
        switch (this.type) {
            case CanvasImageType.PNG:
                this.image = document.createElement('img');
                this.image.src = 'data:image/png;base64,' + reader.readBase64();
                break;
            default:
                console.warn('unsupported image type ' + this.type);
        }
    }

    draw() {
        ctx.drawImage(this.image, this.x, this.y);
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.type);
        writer.writeBase64(this.image.src.substr(this.image.src.indexOf(',') + 1)); // strip data url stuff
    }

    static create(x, y, image) {
        let shape = Object.create(this.prototype);
        shape.x = x;
        shape.y = y;
        shape.type = CanvasImageType.PNG;
        shape.image = image;
        return shape;
    }
}

export const CanvasImageType = {
    PNG:0,
    URL:1
}