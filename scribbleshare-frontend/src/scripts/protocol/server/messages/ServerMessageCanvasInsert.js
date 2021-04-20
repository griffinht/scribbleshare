import CanvasMove from "../../../canvas/CanvasMove.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageCanvasInsert extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.CANVAS_INSERT);
        this.canvasInsertsMap = new Map();
        this.length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            let canvasObjectType = reader.readUint8();
            let canvasMoves = [];
            let lengthJ = reader.readUint8();
            for (let j = 0; j < lengthJ; j++) {
                canvasMoves.push(new CanvasMove(reader));
            }
            this.canvasInsertsMap.set(canvasObjectType, canvasMoves);
        }
    }
}