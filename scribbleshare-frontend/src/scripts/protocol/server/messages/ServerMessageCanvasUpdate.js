import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {getCanvasUpdate} from "../../../canvas/canvasUpdate/getCanvasUpdate.js";

export default class ServerMessageCanvasUpdate extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.CANVAS_UPDATE);
        this.canvasUpdates = [];
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasUpdates.push(getCanvasUpdate(reader.readUint8(), reader));
        }
    }
}