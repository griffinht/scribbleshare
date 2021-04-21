import ServerMessageType from "../ServerMessageType.js";
import ServerMessage from "../ServerMessage.js";
import CanvasInsert from "../../../canvas/CanvasInsert.js";

export default class ServerMessageCanvasInsert extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.CANVAS_INSERT);
        this.canvasInsertsMap = new Map();
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            let canvasObjectType = reader.readUint8();
            let canvasInserts = [];
            let lengthJ = reader.readUint8();
            for (let j = 0; j < lengthJ; j++) {
                canvasInserts.push(new CanvasInsert(canvasObjectType, reader));
            }
            this.canvasInsertsMap.set(canvasObjectType, canvasInserts);
        }
    }
}