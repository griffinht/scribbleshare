import CanvasObject from "../CanvasObject.js";
import {CanvasObjectType} from "../CanvasObjectType.js";

export default class Shape extends CanvasObject {
    constructor(reader) {
        super(CanvasObjectType.SHAPE, reader);
        this.length = reader.readUint16();
        this.width = reader.readUint16();
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeInt16(this.length);
        writer.writeInt16(this.width);
    }
}