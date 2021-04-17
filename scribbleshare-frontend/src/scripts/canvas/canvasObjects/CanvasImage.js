import CanvasObject from "../CanvasObject.js";
import {Canvas, ctx} from "../Canvas.js";
import {apiUrl} from "../../main.js";

export default class CanvasImage extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.image = document.createElement('img');
        this.image.src = apiUrl + '/resource/canvasImage/' + reader.readBigInt64();//todo url
    }

    draw() {
        ctx.drawImage(this.image, this.x, this.y);
    }

    serialize(writer) {
        super.serialize(writer);
        let request = new XMLHttpRequest();
        //todo error handling
        request.open('PUT', apiUrl + '/resource/canvasImage/' + this.id);
        request.send();
        writer.writeUint8(this.type);
        writer.writeBase64(this.image.src.substr(this.image.src.indexOf(',') + 1)); // strip data url stuff
    }

    static create(x, y, image) {
        let shape = Object.create(this.prototype);
        shape.x = x;
        shape.y = y;
        shape.image = image;
        return shape;
    }
}