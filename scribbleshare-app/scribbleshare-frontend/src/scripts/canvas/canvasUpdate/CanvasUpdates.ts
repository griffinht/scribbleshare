import CanvasUpdate from "./CanvasUpdate.js";
import ByteBuffer from "../../protocol/ByteBuffer.js";
import CanvasUpdateType, {getCanvasUpdate} from "./CanvasUpdateType.js";
import CanvasObject from "../canvasObject/CanvasObject.js";
import CanvasUpdateInsert from "./canvasUpdates/CanvasUpdateInsert.js";

export default class CanvasUpdates {
    id: number;
    canvasUpdates: CanvasUpdate[][] = [];

    constructor(byteBuffer: ByteBuffer) {
        this.id = byteBuffer.readInt16();
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            let type: CanvasUpdateType = byteBuffer.readUint8();//todo error checking?
            let array = [];
            let lengthK = byteBuffer.readUint8();
            for (let k = 0; k < lengthK; k++) {
                array.push(getCanvasUpdate(type, byteBuffer));
            }
        }
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeInt16(this.id);
        byteBuffer.writeUint8(this.canvasUpdates.length);
        this.canvasUpdates.forEach((canvasUpdates) => {
            byteBuffer.writeUint8(canvasUpdates[0].getCanvasUpdateType());
            canvasUpdates.forEach((canvasUpdate) => {
                canvasUpdate.serialize(byteBuffer);
            });
        });
    }

    static create(id: number, dt: number, canvasObject: CanvasObject) {
        let object: CanvasUpdates = Object.create(this.prototype);
        object.id = id;
        object.canvasUpdates = [];
        object.canvasUpdates.push([CanvasUpdateInsert.create(dt, canvasObject)]);
        return object;
    }
}