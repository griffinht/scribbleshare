import {getCanvasObject} from "./getCanvasObject.js";

export default class CanvasObjectWrapper {
    constructor(canvasObjectType, canvasObject) {
        this.canvasObjectType = canvasObjectType;
        this.canvasObject = canvasObject;
    }

    serialize(writer) {
        writer.writeUint8(this.canvasObjectType);
        this.canvasObject.serialize(writer);
    }

    static deserialize(reader) {
        let canvasObjectWrapper = Object.create(this.prototype);
        canvasObjectWrapper.canvasObjectType = reader.readUint8();
        canvasObjectWrapper.canvasObject = getCanvasObject(canvasObjectWrapper.canvasObjectType, reader);
        return canvasObjectWrapper;
    }
}