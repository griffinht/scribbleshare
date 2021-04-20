import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import CanvasDelete from "../../../canvas/CanvasDelete.js";

export default class ServerMessageCanvasDelete extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.CANVAS_DELETE);
        this.canvasDeletes = [];
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasDeletes.push(new CanvasDelete(reader));
        }
    }
}