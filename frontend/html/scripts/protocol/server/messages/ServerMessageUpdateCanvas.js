import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {Canvas} from "../../../canvas/Canvas.js";

export default class ServerMessageUpdateCanvas extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.UPDATE_CANVAS);
        this.canvasMap = new Map();
        let length = reader.readUint16();
        for (let i = 0; i < length; i++) {
            this.canvasMap.put(reader.readInt16(), new Canvas(reader));
        }
    }
}