import {getCanvasObject} from "./getCanvasObject.js";
import CanvasObject from "./CanvasObject";
import ByteBuffer from "../../protocol/ByteBuffer";
import CanvasObjectType from "./CanvasObjectType";

export default class CanvasObjectWrapper {
    canvasObjectType: CanvasObjectType;
    canvasObject: CanvasObject;

    constructor(type: CanvasObjectType, canvasObject: CanvasObject) {
        this.canvasObjectType = type;
        this.canvasObject = canvasObject;
    }

    serialize(byteBuffer: ByteBuffer) {//todo implict returns??
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