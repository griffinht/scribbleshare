import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageHandshake extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.HANDSHAKE);
        this.id = reader.readBigInt64();
        this.token = reader.readBigInt64();
    }
}