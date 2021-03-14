export default class CanvasObject {
    constructor(type, reader) {
        this.type = type;
        this.id = reader.readUint16();
    }

    draw(dt) {

    }

    serialize(writer) {
        writer.writeUint8(this.type);
        writer.writeUint16(this.id);
    }
}