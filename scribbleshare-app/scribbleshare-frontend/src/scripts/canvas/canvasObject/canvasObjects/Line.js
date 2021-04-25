import CanvasObject from "../CanvasObject.js";
import {ctx} from "../../Canvas.js";

export default class LineCanvasObject extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.points = [];
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            this.points[i] = new Point(reader);
        }
    }

    draw() {
        ctx.beginPath();
        ctx.moveTo(this.x, this.y);
        this.points.forEach((point) => {
            ctx.lineTo(point.x, point.y);
        });
        ctx.stroke();
    }

    pushPoint(x, y) {
        this.points.push(Point.create(x, y));
    }

    serialize(writer) {
        super.serialize(writer);

        let lastPoint = this.points[0];
        let realPoints = [];
        this.points.forEach((point) => {
            if (Math.sqrt(Math.pow(lastPoint.x - point.x, 2) + Math.pow(lastPoint.y - point.y, 2)) > 10) {
                realPoints.push(point);
            }
        });

        writer.writeUint8(realPoints.length);
        realPoints.forEach((realPoint) => {
            realPoint.serialize(writer);
        });
    }

    static create(x, y) {
        let object = Object.create(this.prototype);
        object.points = [];
        object.x = x;
        object.y = y;
        return object;
    }
}

class Point {
    constructor(reader) {
        this.x = reader.readInt16();
        this.y = reader.readInt16();
    }

    serialize(writer) {
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
    }

    static create(x, y) {
        let object = Object.create(this.prototype);
        object.x = x;
        object.y = y;
        return object;
    }
}