import CanvasObject from "./CanvasObject.js";
import {lerp} from "../Canvas.js";

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

/*    lerp(target, t) {
        super.lerp(target, t);
        this.width = lerp(this.original.width, target.width, t);
        this.height = lerp(this.original.height, target.height, t);
        this.rotation = lerp(this.original.rotation, target.rotation, t);
    }*/



/*    static create(entityCanvasObject) {
        let object = Object.create(this.prototype);
        object.x = entityCanvasObject.x;
        object.y = entityCanvasObject.y;
        object.width = entityCanvasObject.width;
        object.height = entityCanvasObject.height;
        object.rotation = entityCanvasObject.rotation;
        return object;
    }*/
}