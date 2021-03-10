import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageCreateDocument extends ClientMessage {
    constructor() {
        super(ClientMessageType.CREATE_DOCUMENT);
    }
}