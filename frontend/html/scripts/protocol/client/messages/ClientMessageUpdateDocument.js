import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageUpdateDocument extends ClientMessage {
    constructor(canvasObjects) {
        super(ClientMessageType.UPDATE_DOCUMENT);
        this.canvasObjects = canvasObjects;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasObjects.size);
        this.canvasObjects.forEach((value, key) => {
            writer.writeUint8(key);
            writer.writeUint8(value.size);
            value.forEach((v, k) => {
                writer.writeInt16(k);
                v.serialize(writer);
            });
        });
    }
}