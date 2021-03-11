import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageAddClient extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.ADD_CLIENT);
        this.id = reader.readInt16();
        this.userId = reader.readBigInt64();
    }
}