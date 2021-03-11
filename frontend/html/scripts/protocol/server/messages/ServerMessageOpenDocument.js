import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageOpenDocument extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.OPEN_DOCUMENT);
        this.documentId = reader.readBigInt64();
    }
}