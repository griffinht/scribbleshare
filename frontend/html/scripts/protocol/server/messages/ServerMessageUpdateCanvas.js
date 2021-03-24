import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {Canvas} from "../../../canvas/Canvas.js";

export default class ServerMessageUpdateCanvas extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.UPDATE_CANVAS);
        this.canvasMap = new Map();
        for (let i = 0; i < reader.readUint16(); i++) {
            this.canvasMap.put(reader.readInt16(), new Canvas(reader));
        }
    }
}