import socket from "./protocol/WebSocketHandler.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";

const users = new Map();
let user = null;

export function getUser(id) {
    if (id == null) {
        return user;
    } else {
        return users.get(id);
    }
}

class User {
    constructor(id) {
        this.id = id;
        users.set(id, this);
    }
}

socket.addMessageListener(ServerMessageType.HANDSHAKE, (serverMessageHandshake) => {
    user = new User(serverMessageHandshake.userId);
})
socket.addMessageListener(ServerMessageType.ADD_USER, (serverMessageAddUser) => {
    let user = users.get(serverMessageAddUser.user.id);
    if (user == null) {
        user = new User(serverMessageAddUser.user.id);
    }
    Object.assign(user, serverMessageAddUser.user);
});