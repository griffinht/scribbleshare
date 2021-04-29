import {getCanvasObject} from "./getCanvasObject.js";

export default class CanvasObjectWrapper {
    constructor(canvasObjectType, canvasObject) {
        this.canvasObjectType = canvasObjectType;
        this.canvasObject = canvasObject;
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.canvasObjectType);
        this.canvasObject.serialize(byteBuffer);
    }

    static deserialize(byteBuffer: ByteBuffer) {
        let canvasObjectWrapper = Object.create(this.prototype);
        canvasObjectWrapper.canvasObjectType = byteBuffer.readUint8();
        canvasObjectWrapper.canvasObject = getCanvasObject(canvasObjectWrapper.canvasObjectType, byteBuffer);
        return canvasObjectWrapper;
    }
}