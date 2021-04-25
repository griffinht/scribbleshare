import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import MouseMove from "../../../MouseMove.js";

export default class ServerMessageMouseMove extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.MOUSE_MOVE);
        this.client = reader.readInt16();
        this.mouseMoves = [];
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            this.mouseMoves[i] = new MouseMove(reader);
        }
    }

}