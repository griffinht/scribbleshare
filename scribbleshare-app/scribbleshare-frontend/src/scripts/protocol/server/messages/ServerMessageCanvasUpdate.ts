import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";
import CanvasUpdates from "../../../canvas/canvasUpdate/CanvasUpdates.js";

export default class ServerMessageCanvasUpdate extends ServerMessage {
    canvasUpdates: Array<CanvasUpdates>;

    constructor(byteBuffer: ByteBuffer) {
        super();
        this.canvasUpdates = [];
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasUpdates.push(new CanvasUpdates(byteBuffer));
        }
    }

    getType(): ServerMessageType {
        return ServerMessageType.CANVAS_UPDATE;
    }
}