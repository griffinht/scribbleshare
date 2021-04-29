import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {getCanvasUpdate} from "../../../canvas/canvasUpdate/getCanvasUpdate.js";
import ByteBuffer from "../../ByteBuffer";
import CanvasUpdate from "../../../canvas/canvasUpdate/CanvasUpdate";

export default class ServerMessageCanvasUpdate extends ServerMessage {
    canvasUpdates: Array<CanvasUpdate>;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.CANVAS_UPDATE);
        this.canvasUpdates = [];
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasUpdates.push(getCanvasUpdate(byteBuffer.readUint8(), byteBuffer));
        }
    }
}