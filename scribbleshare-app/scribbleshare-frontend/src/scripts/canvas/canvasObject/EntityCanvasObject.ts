import CanvasObject from "./CanvasObject.js";
import ByteBuffer from "../../protocol/ByteBuffer";

export default class EntityCanvasObject extends CanvasObject {
    width: number;
    height: number;
    rotation: number;

    constructor(byteBuffer: ByteBuffer) {
        super(byteBuffer);
        this.width = byteBuffer.readInt16();
        this.height = byteBuffer.readInt16();
        this.rotation = byteBuffer.readUint8();
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeInt16(this.width);
        byteBuffer.writeInt16(this.height);
        byteBuffer.writeUint8(this.rotation);
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