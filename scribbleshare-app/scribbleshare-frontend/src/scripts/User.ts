import socket from "./protocol/WebSocketHandler.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";
import ServerMessageAddUser from "./protocol/server/messages/ServerMessageAddUser";

const users = new Map<bigint, User>();

export function getUser(id: bigint) {
    return users.get(id);
}

class User {
    id: bigint;

    constructor(id: bigint) {
        this.id = id;
        users.set(id, this);
    }
}

socket.addMessageListener(ServerMessageType.ADD_USER, (serverMessageAddUser: ServerMessageAddUser) => {
    let user = users.get(serverMessageAddUser.user);
    if (user == null) {
        user = new User(serverMessageAddUser.user);
        users.set(user.id, user);
    }
    //todo update with other user properties
});