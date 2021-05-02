import ClientMessage from "../ClientMessage.js";
import ClientMessageType from "../ClientMessageType.js";
import CanvasUpdate from "../../../canvas/canvasUpdate/CanvasUpdate";
import ByteBuffer from "../../ByteBuffer";

export default class ClientMessageCanvasUpdate extends ClientMessage {
    canvasUpdates: Array<CanvasUpdate>;
    constructor(canvasUpdates: Array<CanvasUpdate>) {
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
