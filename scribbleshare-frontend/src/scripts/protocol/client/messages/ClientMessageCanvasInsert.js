import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageCanvasInsert extends ClientMessage {
    constructor(canvasInsertsMap) {
        super(ClientMessageType.CANVAS_INSERT);
        this.canvasInsertsMap = canvasInsertsMap;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasInsertsMap.size);
        this.canvasInsertsMap.forEach((canvasInserts, canvasObjectType) => {
            writer.writeUint8(canvasObjectType);
            writer.writeUint8(canvasInserts.length);
            canvasInserts.forEach((canvasInsert) => {
                canvasInsert.serialize(writer);
            });
        });
    }
}