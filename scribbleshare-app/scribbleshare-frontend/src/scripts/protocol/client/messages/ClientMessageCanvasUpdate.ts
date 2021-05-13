import ClientMessage from "../ClientMessage.js";
import ClientMessageType from "../ClientMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";
import CanvasUpdates from "../../../canvas/canvasUpdate/CanvasUpdates";

export default class ClientMessageCanvasUpdate extends ClientMessage {
    canvasUpdatesArray: CanvasUpdates[];
    constructor(canvasUpdatesArray: CanvasUpdates[]) {
        super();
        this.canvasUpdatesArray = canvasUpdatesArray;
    }

    getType(): ClientMessageType {
        return ClientMessageType.CANVAS_UPDATE;
    }
    
    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeUint8(this.canvasUpdatesArray.length);
        this.canvasUpdatesArray.forEach((canvasUpdates) => {
            canvasUpdates.serialize(byteBuffer);
        })
    }
}
