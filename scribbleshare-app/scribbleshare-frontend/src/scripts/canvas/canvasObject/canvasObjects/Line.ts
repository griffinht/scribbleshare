import CanvasObject from "../CanvasObject.js";
import {ctx} from "../../Canvas.js";

export default class LineCanvasObject extends CanvasObject {
    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.red = byteBuffer.readUint8();
        this.green = byteBuffer.readUint8();
        this.blue = byteBuffer.readUint8();
        this.points = [];
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            this.points[i] = new Point(byteBuffer);
        }
    }

    draw() {
        ctx.fillStyle = 'rgb(' + this.red + ',' + this.green + ',' + this.blue + ')';
        ctx.strokeStyle = ctx.fillStyle;
        ctx.beginPath();
        ctx.moveTo(this.x, this.y);
        this.points.forEach((point) => {
            ctx.lineTo(point.x, point.y);
        });
        ctx.stroke();
        ctx.strokeStyle = '#000';
    }

    pushPoint(x, y) {
        this.points.push(Point.create(x, y));
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeUint8(this.red);
        byteBuffer.writeUint8(this.green);
        byteBuffer.writeUint8(this.blue);
        let lastPoint = this.points[0];
        let realPoints = [];
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

    static create(x, y, color) {
        let object = Object.create(this.prototype);
        object.points = [];
        object.x = x;
        object.y = y;
        object.red = color.r;
        object.blue = color.b;
        object.green = color.g;
        return object;
    }
}

class Point {
    constructor(byteBuffer: ByteBuffer) {
        this.x = byteBuffer.readInt16();
        this.y = byteBuffer.readInt16();
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeInt16(this.x);
        byteBuffer.writeInt16(this.y);
    }

    static create(x, y) {
        let object = Object.create(this.prototype);
        object.x = x;
        object.y = y;
        return object;
    }
}