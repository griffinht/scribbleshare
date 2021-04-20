import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageCanvasInsert extends ClientMessage {
    constructor(canvasObjects) {
        super(ClientMessageType.CANVAS_INSERT);
        this.canvasObjectWrappers = canvasObjects;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasObjectWrappers.size);
        this.canvasObjectWrappers.forEach((canvasInserts, canvasObjectType) => {
            writer.writeUint8(canvasObjectType);
            writer.writeUint8(canvasInserts.size);
            canvasInserts.forEach((canvasInsert) => {
                canvasInsert.serialize(writer);
            });
        });
    }
}