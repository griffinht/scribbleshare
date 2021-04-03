import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageRemoveClient extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.REMOVE_CLIENT);
        this.id = reader.readInt16();
    }
}