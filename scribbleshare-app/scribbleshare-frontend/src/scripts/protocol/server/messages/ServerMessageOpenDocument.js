import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {Canvas} from "../../../canvas/Canvas.js";

export default class ServerMessageOpenDocument extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.OPEN_DOCUMENT);
        this.id = reader.readBigInt64();
        this.canvas = new Canvas(reader);
    }
}