import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageCanvasDelete extends ClientMessage {
    constructor(canvasDeletes) {
        super(ClientMessageType.CANVAS_DELETE);
        this.canvasDeletes = canvasDeletes;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasDeletes.size);
        this.canvasDeletes.forEach((canvasDelete) => {
            canvasDelete.serialize(writer);
        });
    }
}