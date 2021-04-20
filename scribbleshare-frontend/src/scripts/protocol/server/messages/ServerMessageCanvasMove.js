import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import CanvasMove from "../../../canvas/CanvasMove.js";

export default class ServerMessageCanvasMove extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.CANVAS_MOVE)
        this.canvasMovesMap = new Map();
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            let id = reader.readInt16();
            let canvasMoves = [];
            let lengthJ = reader.readUint8();
            for (let j = 0; j < lengthJ; j++) {
                canvasMoves.push(new CanvasMove(reader));
            }
            this.canvasMovesMap.set(id, canvasMoves);
        }
    }
}