import socket from "./protocol/WebSocketHandler.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";

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

socket.addMessageListener(ServerMessageType.ADD_USER, (serverMessageAddUser: ServerMessageType) => {
    let user = users.get(serverMessageAddUser.user.id);
    if (user == null) {
        user = new User(serverMessageAddUser.user.id);
    }
    Object.assign(user, serverMessageAddUser.user);
});