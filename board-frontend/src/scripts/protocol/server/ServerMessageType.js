import ServerMessageAddClient from "./messages/ServerMessageAddClient.js";
import ServerMessageRemoveClient from "./messages/ServerMessageRemoveClient.js";
import ServerMessageUpdateCanvas from "./messages/ServerMessageUpdateCanvas.js";
import ServerMessageOpenDocument from "./messages/ServerMessageOpenDocument.js";
import ServerMessageHandshake from "./messages/ServerMessageHandshake.js";
import ServerMessageAddUser from "./messages/ServerMessageAddUser.js";
import ServerMessageDeleteDocument from "./messages/ServerMessageDeleteDocument.js";
import ServerMessageUpdateDocument from "./messages/ServerMessageUpdateDocument.js";
import ServerMessageGetInvite from "./messages/ServerMessageGetInvite.js";

const ServerMessageType = {
    ADD_CLIENT:0,
    REMOVE_CLIENT:1,
    UPDATE_CANVAS:2,
    OPEN_DOCUMENT:3,
    UPDATE_DOCUMENT:4,
    HANDSHAKE:5,
    ADD_USER:6,
    DELETE_DOCUMENT:7,
    GET_INVITE:8,
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
        case ServerMessageType.UPDATE_CANVAS:
            message = new ServerMessageUpdateCanvas(reader);
            break;
        case ServerMessageType.OPEN_DOCUMENT:
            message = new ServerMessageOpenDocument(reader);
            break;
        case ServerMessageType.UPDATE_DOCUMENT:
            message = new ServerMessageUpdateDocument(reader);
            break;
        case ServerMessageType.HANDSHAKE:
            message = new ServerMessageHandshake(reader);
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
        default:
            console.error('unknown payload type ' + type + ', offset ' + reader.position + ', event ', event);
            break;
    }
    return message;
}