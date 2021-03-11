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

socket.addMessageListener(ServerMessageType.HANDSHAKE, (event) => {
    user = new User(event.userId);
})
socket.addMessageListener(ServerMessageType.ADD_USER, (event) => {
    let user = users.get(event.user.id);
    if (user == null) {
        user = new User(event.user.id);
    }
    Object.assign(user, event.user);
});