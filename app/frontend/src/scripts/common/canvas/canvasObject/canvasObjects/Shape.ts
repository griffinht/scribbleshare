import {ctx} from "../../Canvas.js";
import EntityCanvasObject from "../EntityCanvasObject.js";
import ByteBuffer from "../../../protocol/ByteBuffer.js";
import Color from "../../../Color.js";
import CanvasObjectType from "../CanvasObjectType.js";

export default class Shape extends EntityCanvasObject {
    type: ShapeType;
    color: Color;

    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.type = byteBuffer.readUint8();
        this.color = Color.deserialize(byteBuffer);
    }

    getCanvasObjectType() {
        return CanvasObjectType.SHAPE;
    }

    draw() {
        ctx.fillStyle = this.color.getRgb();
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
        this.color.serialize(byteBuffer)
    }

    static create(x: number, y: number, width: number, height: number, type: ShapeType, color: Color) {
        let object: Shape = Object.create(this.prototype);
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

export enum ShapeType {
    RECTANGLE,
    ELLIPSE,
    TRIANGLE,
}