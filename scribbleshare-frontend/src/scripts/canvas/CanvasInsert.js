import {getCanvasObject} from "./Canvas.js";

export default class CanvasInsert {
    constructor(canvasObjectType, reader) {
        this.dt = reader.readUint8();
        this.id = reader.readInt16();
        this.canvasObject = getCanvasObject(canvasObjectType, reader);
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.id);
        this.canvasObject.serialize(writer);
    }
}