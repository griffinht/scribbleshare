import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageUpdateCanvas extends ClientMessage {
    constructor(canvasObjects) {
        super(ClientMessageType.UPDATE_CANVAS);
        this.canvasObjectWrappers = canvasObjects;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasObjectWrappers.size);
        this.canvasObjectWrappers.forEach((map, canvasObjectType) => {
            writer.writeUint8(canvasObjectType);
            writer.writeUint16(map.size);
            map.forEach((canvasObjectWrappers, id) => {
                writer.writeInt16(id);
                writer.writeUint8(canvasObjectWrappers.length);
                canvasObjectWrappers.forEach((canvasObjectWrapper) => {
                    canvasObjectWrapper.serialize(writer);
                });
            });
        });
    }
}