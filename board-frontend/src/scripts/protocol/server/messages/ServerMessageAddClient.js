import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageAddClient extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.ADD_CLIENT);
        this.clients = [];
        let length = reader.readUint16();
        for (let i = 0; i < length; i++) {
            this.clients[i] = {id:reader.readInt16(), userId:reader.readBigInt64()};
        }
    }
}