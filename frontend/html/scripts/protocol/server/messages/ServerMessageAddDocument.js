import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageAddDocument extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.ADD_DOCUMENT);
        this.id = reader.readBigInt64();
        this.name = reader.readString();
    }
}