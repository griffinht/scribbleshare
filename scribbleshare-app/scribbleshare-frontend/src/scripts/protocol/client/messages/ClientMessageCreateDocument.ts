import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageCreateDocument extends ClientMessage {
    getType(): ClientMessageType {
        return ClientMessageType.CREATE_DOCUMENT;
    }
}