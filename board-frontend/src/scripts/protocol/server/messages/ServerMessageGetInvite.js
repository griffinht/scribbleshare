import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageGetInvite extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.GET_INVITE);
        this.code = reader.readString();
    }
}