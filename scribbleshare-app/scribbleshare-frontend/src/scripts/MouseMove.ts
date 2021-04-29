import BufferReader from "./protocol/BufferReader";
import BufferWriter from "./protocol/BufferWriter";

export default class MouseMove {
    dt: number;
    x: number;
    y: number;

    constructor(reader: BufferReader) {
        this.dt = reader.readUint8();
        this.x = reader.readInt16();
        this.y = reader.readInt16();
    }

    serialize(writer: BufferWriter) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
    }

    static create(dt: number, x: number, y: number) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.x = x;
        object.y = y;
        return object;
    }
}