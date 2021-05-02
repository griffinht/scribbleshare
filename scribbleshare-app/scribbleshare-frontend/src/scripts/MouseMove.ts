import ByteBuffer from "./protocol/ByteBuffer.js";

export default class MouseMove {
    dt: number;
    x: number;
    y: number;

    constructor(byteBuffer: ByteBuffer) {
        this.dt = byteBuffer.readUint8();
        this.x = byteBuffer.readInt16();
        this.y = byteBuffer.readInt16();
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.dt);
        byteBuffer.writeInt16(this.x);
        byteBuffer.writeInt16(this.y);
    }

    static create(dt: number, x: number, y: number) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.x = x;
        object.y = y;
        return object;
    }
}