import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageUpdateCanvas extends ClientMessage {
    constructor(canvasObjects) {
        super(ClientMessageType.UPDATE_CANVAS);
        this.canvasObjects = canvasObjects;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasObjects.size);
        console.log(this.canvasObjects);
        this.canvasObjects.forEach((value, key) => {
            writer.writeUint8(key);
            writer.writeUint16(value.size);
            value.forEach((v, k) => {
                writer.writeInt16(k);
                v.serialize(writer);
            });
        });
    }
}