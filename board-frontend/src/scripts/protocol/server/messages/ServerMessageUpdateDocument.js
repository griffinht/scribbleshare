import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageUpdateDocument extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.UPDATE_DOCUMENT);
        this.shared = reader.readUint8() ? 1 : 0;
        this.id = reader.readBigInt64();
        this.name = reader.readString8();
    }
}