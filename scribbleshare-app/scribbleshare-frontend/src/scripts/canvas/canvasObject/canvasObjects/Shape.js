import {ctx} from "../../Canvas.js";
import EntityCanvasObject from "../EntityCanvasObject.js";

export default class Shape extends EntityCanvasObject {
    constructor(reader) {
        super(reader);
        this.type = reader.readUint8();
        this.red = reader.readUint8();
        this.green = reader.readUint8();
        this.blue = reader.readUint8();
    }

    draw() {
        ctx.fillStyle = 'rgb(' + this.red + ',' + this.green + ',' + this.blue + ')';
        ctx.strokeStyle = ctx.fillStyle;
        switch (this.type) {
            case ShapeType.RECTANGLE:
                ctx.fillRect(0, 0, this.width, this.height);
                break;
            case ShapeType.ELLIPSE:
                ctx.beginPath();
                ctx.ellipse(this.width / 2, this.height / 2, this.width / 2, this.height / 2, 0, 0, Math.PI * 2);
                ctx.fill();
                ctx.stroke();
                break;
        }
        ctx.strokeStyle = '#000';
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.type);
        writer.writeUint8(this.red);
        writer.writeUint8(this.green);
        writer.writeUint8(this.blue);
    }

    static create(x, y, width, height, type, color) {
        let object = Object.create(this.prototype);
        object.x = x;
        object.y = y;
        object.width = width;
        object.height = height;
        object.rotation = 0;
        object.type = type;
        object.red = color.r;
        object.blue = color.b;
        object.green = color.g;
        return object;
    }
}

export const ShapeType = {
    RECTANGLE:0,
    ELLIPSE:1,
}