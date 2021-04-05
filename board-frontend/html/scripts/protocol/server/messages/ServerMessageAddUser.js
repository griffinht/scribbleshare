import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageAddUser extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.ADD_USER);
        this.user = {};
        this.user.id = reader.readBigInt64();
    }
}