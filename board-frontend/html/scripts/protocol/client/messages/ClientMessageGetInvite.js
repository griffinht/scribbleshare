import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageGetInvite extends ClientMessage {
    constructor() {
        super(ClientMessageType.GET_INVITE);
    }

    getBufferSize() {
        return super.getBufferSize();
    }

    serialize(writer) {
        super.serialize(writer);
    }
}