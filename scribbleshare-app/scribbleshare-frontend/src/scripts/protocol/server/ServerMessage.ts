import ServerMessageType from "./ServerMessageType.js";

export default abstract class ServerMessage {
    abstract getType(): ServerMessageType;
}
