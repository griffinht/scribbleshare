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
import BufferReader from "../BufferReader";

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


export function getServerMessage(type: ServerMessageType, reader: BufferReader) {
    let message;
    switch (type) {
        case ServerMessageType.ADD_CLIENT:
            message = new ServerMessageAddClient(reader);
            break;
        case ServerMessageType.REMOVE_CLIENT:
            message = new ServerMessageRemoveClient(reader);
            break;
        case ServerMessageType.CANVAS_UPDATE:
            message = new ServerMessageCanvasUpdate(reader);
            break;
        case ServerMessageType.UPDATE_DOCUMENT:
            message = new ServerMessageUpdateDocument(reader);
            break;
        case ServerMessageType.ADD_USER:
            message = new ServerMessageAddUser(reader)
            break;
        case ServerMessageType.DELETE_DOCUMENT:
            message = new ServerMessageDeleteDocument(reader);
            break;
        case ServerMessageType.GET_INVITE:
            message = new ServerMessageGetInvite(reader);
            break;
        case ServerMessageType.OPEN_DOCUMENT:
            message = new ServerMessageOpenDocument(reader);
            break;
        case ServerMessageType.HANDSHAKE:
            message = new ServerMessageHandshake(reader);
            break;
        case ServerMessageType.MOUSE_MOVE:
            message = new ServerMessageMouseMove(reader);
            break;
        default:
            console.error('unknown payload type ' + type + ', offset ' + reader.position + ', event ', event);
            break;
    }
    return message;
}