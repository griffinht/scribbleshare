import ServerMessageAddClient from "./messages/ServerMessageAddClient.js";
import ServerMessageRemoveClient from "./messages/ServerMessageRemoveClient.js";
import ServerMessageAddUser from "./messages/ServerMessageAddUser.js";
import ServerMessageDeleteDocument from "./messages/ServerMessageDeleteDocument.js";
import ServerMessageUpdateDocument from "./messages/ServerMessageUpdateDocument.js";
import ServerMessageGetInvite from "./messages/ServerMessageGetInvite.js";
import ServerMessageOpenDocument from "./messages/ServerMessageOpenDocument.js";
import ServerMessageCanvasUpdate from "./messages/ServerMessageCanvasUpdate.js";
import ServerMessageHandshake from "./messages/ServerMessageHandshake.js";
import ServerMessageMouseMove from "./messages/ServerMessageMouseMove.js";
import ByteBuffer from "../ByteBuffer";
import ServerMessage from "./ServerMessage";

enum ServerMessageType {
    ADD_CLIENT,
    REMOVE_CLIENT,
    UPDATE_DOCUMENT,
    ADD_USER,
    DELETE_DOCUMENT,
    GET_INVITE,
    OPEN_DOCUMENT,
    CANVAS_UPDATE,
    HANDSHAKE,
    MOUSE_MOVE,
}
export default ServerMessageType;

export function getServerMessage(type: ServerMessageType, byteBuffer: ByteBuffer): ServerMessage {
    let message;
    switch (type) {
        case ServerMessageType.ADD_CLIENT:
            message = new ServerMessageAddClient(byteBuffer);
            break;
        case ServerMessageType.REMOVE_CLIENT:
            message = new ServerMessageRemoveClient(byteBuffer);
            break;
        case ServerMessageType.CANVAS_UPDATE:
            message = new ServerMessageCanvasUpdate(byteBuffer);
            break;
        case ServerMessageType.UPDATE_DOCUMENT:
            message = new ServerMessageUpdateDocument(byteBuffer);
            break;
        case ServerMessageType.ADD_USER:
            message = new ServerMessageAddUser(byteBuffer)
            break;
        case ServerMessageType.DELETE_DOCUMENT:
            message = new ServerMessageDeleteDocument(byteBuffer);
            break;
        case ServerMessageType.GET_INVITE:
            message = new ServerMessageGetInvite(byteBuffer);
            break;
        case ServerMessageType.OPEN_DOCUMENT:
            message = new ServerMessageOpenDocument(byteBuffer);
            break;
        case ServerMessageType.HANDSHAKE:
            message = new ServerMessageHandshake(byteBuffer);
            break;
        case ServerMessageType.MOUSE_MOVE:
            message = new ServerMessageMouseMove(byteBuffer);
            break;
        default:
            throw new Error('unknown payload type ' + type + ', offset ' + byteBuffer.position);
    }
    return message;
}