import ClientMessage from "../ClientMessage.js";
import ClientMessageType from "../ClientMessageType.js";

export default class ClientMessageCanvasUpdate extends ClientMessage {
    constructor(canvasUpdates) {
        super(ClientMessageType.CANVAS_UPDATE);
        this.canvasUpdates = canvasUpdates;
    }
    
    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeUint8(this.canvasUpdates.length);
        this.canvasUpdates.forEach((canvasUpdate) => {
            canvasUpdate.serialize(byteBuffer);
        })
    }
}
