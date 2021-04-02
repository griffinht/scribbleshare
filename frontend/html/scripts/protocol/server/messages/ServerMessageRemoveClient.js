import ServerMessage from "../ServerMessage.js";

export default class ServerMessageRemoveClient extends ServerMessage {
    constructor(reader) {
        super(reader);
        this.id = reader.readInt16();
    }
}