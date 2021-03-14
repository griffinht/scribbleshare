import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {Canvas} from "../../../canvas/Canvas.js";

export default class ServerMessageUpdateDocument extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.UPDATE_DOCUMENT);
        this.canvas = new Canvas(reader);
    }
}