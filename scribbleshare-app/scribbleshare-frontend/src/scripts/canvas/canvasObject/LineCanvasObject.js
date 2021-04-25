import CanvasObject from "./CanvasObject.js";

export default class LineCanvasObject extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.points = [];
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
             this.points[i] = new Point(reader);
        }
    }

    serialize(writer) {
        writer.writeUint8();
        this.points.forEach((point) => {
            point.serialize(writer);
        })
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
}