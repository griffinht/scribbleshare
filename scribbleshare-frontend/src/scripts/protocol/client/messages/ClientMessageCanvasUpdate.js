import ClientMessage from "../ClientMessage.js";
import ClientMessageType from "../ClientMessageType.js";

export default class ClientMessageCanvasUpdate extends ClientMessage {
    constructor(canvasUpdates) {
        super(ClientMessageType.CANVAS_UPDATE);
        this.canvasUpdates = canvasUpdates;
    }
    
    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasUpdates.length);
        this.canvasUpdates.forEach((canvasUpdate) => {
            canvasUpdate.serialize(writer);
        })
    }
}
