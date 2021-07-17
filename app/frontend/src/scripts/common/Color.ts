import ByteBuffer from "./protocol/ByteBuffer.js";

export default class Color {
    red: number;
    green: number;
    blue: number;

    constructor(byteBuffer: ByteBuffer) {
        this.red = byteBuffer.readUint8();
        this.green = byteBuffer.readUint8();
        this.blue = byteBuffer.readUint8();
    }

    getRgb() {
        return 'rgb(' + this.red + ',' + this.green + ',' + this.blue + ')';
    }

    update(color: Color) {
        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
    }

    set(red: number, green: number, blue: number) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    copy() {
        return Color.from(this);
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.red);
        byteBuffer.writeUint8(this.green);
        byteBuffer.writeUint8(this.blue);
    }

    static create(red: number, green: number, blue: number) {
        let object: Color = Object.create(this.prototype);
        object.set(red, green, blue);
        return object;
    }

    static from(color: Color) {
        let object: Color = Object.create(this.prototype);
        object.update(color);
        return object;
    }

    static deserialize(byteBuffer: ByteBuffer) {
        return Color.create(byteBuffer.readUint8(), byteBuffer.readUint8(), byteBuffer.readUint8());
    }
}