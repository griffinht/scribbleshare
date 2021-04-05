import {getCanvasObject} from "./Canvas.js";

export default class CanvasObjectWrapper {
    constructor(type, reader) {
        this.dt = reader.readUint8();
        this.canvasObject = getCanvasObject(type, reader);
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        this.canvasObject.serialize(writer);
    }

    static create(dt, canvasObject) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.canvasObject = canvasObject;
        return object;
    }
}