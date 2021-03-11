import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageHandshake extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.HANDSHAKE);
        this.token = reader.readBigInt64();
        this.userId = reader.readBigInt64();
    }
}