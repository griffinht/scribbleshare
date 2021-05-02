import ServerMessageType from "./ServerMessageType.js";

export default class ServerMessage {
    type: ServerMessageType;

    constructor(type: ServerMessageType) {
        this.type = type;
    }
}
