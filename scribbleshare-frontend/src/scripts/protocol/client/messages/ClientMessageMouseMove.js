import ClientMessage from "../ClientMessage.js";
import ClientMessageType from "../ClientMessageType.js";

export default class ClientMessageMouseMove extends ClientMessage {
    constructor(mouseMoves) {
        super(ClientMessageType.MOUSE_MOVE);
        this.mouseMoves = mouseMoves;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.mouseMoves.length);
        for (let i = 0; i < this.mouseMoves.length; i++) {
            this.mouseMoves[i].serialize(writer);
        }
    }
}