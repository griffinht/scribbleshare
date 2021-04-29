import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageHandshake extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.HANDSHAKE);
        this.client = reader.readInt16();
    }
}