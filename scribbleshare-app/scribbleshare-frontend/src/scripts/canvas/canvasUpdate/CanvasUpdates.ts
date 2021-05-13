import CanvasUpdate from "./CanvasUpdate.js";
import CanvasObjectWrapper from "../canvasObject/CanvasObjectWrapper.js";
import ByteBuffer from "../../protocol/ByteBuffer.js";
import {getCanvasUpdate} from "./CanvasUpdateType.js";

export default class CanvasUpdates {
    canvasObjectWrapper: CanvasObjectWrapper;
    canvasUpdates: CanvasUpdate[] = [];

    constructor(byteBuf: ByteBuffer) {
        this.canvasObjectWrapper = CanvasObjectWrapper.deserialize(byteBuf);
        let length = byteBuf.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasUpdates[i] = getCanvasUpdate(byteBuf.readUint8(), byteBuf);
        }
    }

    serialize(byteBuffer: ByteBuffer) {
        this.canvasObjectWrapper.serialize(byteBuffer);
        this.canvasUpdates.forEach((canvasUpdate) => {
            canvasUpdate.serialize(byteBuffer);
        });
    }

    static create(canvasObjectWrapper: CanvasObjectWrapper) {
        let object: CanvasUpdates = Object.create(this.prototype);
        object.canvasObjectWrapper = canvasObjectWrapper;
        object.canvasUpdates = [];
        return object;
    }
}