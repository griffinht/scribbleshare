import CanvasObject from "./CanvasObject.js";

export default class EntityCanvasObject extends CanvasObject {
    constructor(reader) {
        super(reader);
        this.width = reader.readInt16();
        this.height = reader.readInt16();
        this.rotation = reader.readUint8();
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeInt16(this.width);
        writer.writeInt16(this.height);
        writer.writeUint8(this.rotation);
    }
}