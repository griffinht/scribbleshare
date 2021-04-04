import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {Canvas} from "../../../canvas/Canvas.js"
import {CanvasObjectType} from "../../../canvas/CanvasObjectType.js";
import CanvasObject from "../../../canvas/CanvasObject.js";

export default class ServerMessageOpenDocument extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.OPEN_DOCUMENT);
        this.documentId = reader.readBigInt64();
        this.canvas = new Canvas(reader);
    }
}