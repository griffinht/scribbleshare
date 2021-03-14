import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageUpdateDocument extends ClientMessage {
    constructor(canvas) {
        super(ClientMessageType.UPDATE_DOCUMENT);
        this.canvas = canvas;
    }

    serialize(writer) {
        super.serialize(writer);
        this.canvas.serialize(writer);
    }
}