import ClientMessage from "../ClientMessage.js";
import ClientMessageType from "../ClientMessageType.js";
import ByteBuffer from "../../ByteBuffer";
import CanvasUpdates from "../../../canvas/canvasUpdate/CanvasUpdates";

export default class ClientMessageCanvasUpdate extends ClientMessage {
    canvasUpdatesArray: CanvasUpdates[];
    constructor(canvasUpdatesArray: CanvasUpdates[]) {
        super(ClientMessageType.CANVAS_UPDATE);
        this.canvasUpdatesArray = canvasUpdatesArray;
    }
    
    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeUint8(this.canvasUpdatesArray.length);
        this.canvasUpdatesArray.forEach((canvasUpdates) => {
            canvasUpdates.serialize(byteBuffer);
        })
    }
}
