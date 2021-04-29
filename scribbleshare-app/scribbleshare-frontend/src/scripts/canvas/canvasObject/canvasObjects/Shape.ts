import {ctx} from "../../Canvas.js";
import EntityCanvasObject from "../EntityCanvasObject.js";
import ByteBuffer from "../../../protocol/ByteBuffer";
import Color from "../../../Color";

export default class Shape extends EntityCanvasObject {
    type: CanvasObjectType;
    red: number;
    green: number;
    blue: number;

    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.type = byteBuffer.readUint8();
        this.red = byteBuffer.readUint8();
        this.green = byteBuffer.readUint8();
        this.blue = byteBuffer.readUint8();
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
            case ShapeType.TRIANGLE:
                ctx.beginPath();
                ctx.moveTo(this.width, this.height);
                ctx.lineTo(this.width / 2, 0);
                ctx.lineTo(0, this.height);
                ctx.fill();
                ctx.stroke();
        }
        ctx.strokeStyle = '#000';
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeUint8(this.type);
        byteBuffer.writeUint8(this.red);
        byteBuffer.writeUint8(this.green);
        byteBuffer.writeUint8(this.blue);
    }

    static create(x: number, y: number, width: number, height: number, type: CanvasObjectType, color: Color) {
        let object = Object.create(this.prototype);
        object.x = x;
        object.y = y;
        object.width = width;
        object.height = height;
        object.rotation = 0;
        object.type = type;
        object.color = color;
        return object;
    }
}

export const ShapeType = {
    RECTANGLE:0,
    ELLIPSE:1,
    TRIANGLE:2,
}