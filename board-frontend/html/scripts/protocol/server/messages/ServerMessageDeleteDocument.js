import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageDeleteDocument extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.DELETE_DOCUMENT);
        this.id = reader.readBigInt64();
    }
}