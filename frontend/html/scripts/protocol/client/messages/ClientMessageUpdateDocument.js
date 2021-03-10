import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageUpdateDocument extends ClientMessage {
    constructor() {
        super(ClientMessageType.UPDATE_DOCUMENT);

    }

}