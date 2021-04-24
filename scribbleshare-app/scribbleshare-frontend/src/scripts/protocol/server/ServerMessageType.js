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

const ServerMessageType = {
    ADD_CLIENT:0,
    REMOVE_CLIENT:1,
    UPDATE_DOCUMENT:2,
    ADD_USER:3,
    DELETE_DOCUMENT:4,
    GET_INVITE:5,
    OPEN_DOCUMENT:6,
    CANVAS_UPDATE:7,
    HANDSHAKE:8,
    MOUSE_MOVE:9,
};
export default ServerMessageType;


export function getServerMessage(type, reader) {
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