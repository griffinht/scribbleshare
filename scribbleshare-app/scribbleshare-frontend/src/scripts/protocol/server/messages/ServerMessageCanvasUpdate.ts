import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import ByteBuffer from "../../ByteBuffer.js";
import CanvasUpdate from "../../../canvas/canvasUpdate/CanvasUpdate.js";
import {getCanvasUpdate} from "../../../canvas/canvasUpdate/CanvasUpdateType.js";

export default class ServerMessageCanvasUpdate extends ServerMessage {
    canvasUpdates: Array<CanvasUpdate>;

    constructor(byteBuffer: ByteBuffer) {
        super();
        this.canvasUpdates = [];
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasUpdates.push(getCanvasUpdate(byteBuffer.readUint8(), byteBuffer));
        }
    }

    getType(): ServerMessageType {
        return ServerMessageType.CANVAS_UPDATE;
    }
}