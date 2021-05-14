import CanvasObject from "../CanvasObject.js";
import {ctx} from "../../Canvas.js";
import ByteBuffer from "../../../protocol/ByteBuffer.js";
import Color from "../../../Color.js";
import color from "../../../ColorSelector.js";
import CanvasObjectType from "../CanvasObjectType.js";

export default class LineCanvasObject extends CanvasObject {
    color: Color;
    points: Point[];

    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.color = new Color(byteBuffer);
        this.points = [];
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            this.points[i] = new Point(byteBuffer);
        }
    }

    getCanvasObjectType() {
        return CanvasObjectType.LINE;
    }

    draw() {
        ctx.fillStyle = this.color.getRgb();
        ctx.strokeStyle = ctx.fillStyle;
        ctx.beginPath();
        ctx.moveTo(this.x, this.y);
        this.points.forEach((point) => {
            ctx.lineTo(point.x, point.y);
        });
        ctx.stroke();
        ctx.strokeStyle = '#000';
    }

    pushPoint(x: number, y: number) {
        this.points.push(Point.create(x, y));
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        color.serialize(byteBuffer);
        let lastPoint = this.points[0];
        let realPoints: Point[] = [];
        this.points.forEach((point) => {
            if (Math.sqrt(Math.pow(lastPoint.x - point.x, 2) + Math.pow(lastPoint.y - point.y, 2)) > 10) {
                realPoints.push(point);
            }
        });

        byteBuffer.writeUint8(realPoints.length);
        realPoints.forEach((realPoint) => {
            realPoint.serialize(byteBuffer);
        });
    }

    static create(x: number, y: number, color: Color): LineCanvasObject {
        let object = Object.create(this.prototype);
        object.points = [];
        object.x = x;
        object.y = y;
        object.color = Color.from(color);
        return object;
    }
}

class Point {
    x: number;
    y: number;

    constructor(byteBuffer: ByteBuffer) {
        this.x = byteBuffer.readInt16();
        this.y = byteBuffer.readInt16();
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeInt16(this.x);
        byteBuffer.writeInt16(this.y);
    }

    static create(x: number, y: number) {
        let object = Object.create(this.prototype);
        object.x = x;
        object.y = y;
        return object;
    }
}