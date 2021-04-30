import ServerMessageType from "./ServerMessageType";

export default class ServerMessage {
    type: ServerMessageType;

    constructor(type: ServerMessageType) {
        this.type = type;
    }
}
