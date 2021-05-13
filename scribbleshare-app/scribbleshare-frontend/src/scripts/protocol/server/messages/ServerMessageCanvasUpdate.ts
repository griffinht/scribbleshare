import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {deserialize} from "../../../canvas/canvasUpdate/deserialize.js";
import ByteBuffer from "../../ByteBuffer.js";
import CanvasUpdate from "../../../canvas/canvasUpdate/CanvasUpdate.js";

export default class ServerMessageCanvasUpdate extends ServerMessage {
    canvasUpdates: Array<CanvasUpdate>;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.CANVAS_UPDATE);
        this.canvasUpdates = [];
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasUpdates.push(deserialize(byteBuffer.readUint8(), byteBuffer));
        }
    }
}