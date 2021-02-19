import socket from "./WebSocketHandler.js";

const users = new Map();

export function getUser(id) {
    return users.get(id);
}

class User {
    constructor(id) {
        this.id = id;
        users.set(id, this);
    }
}

socket.addEventListener('protocol.adduser', (event) => {
    let user = users.get(event.user.id);
    if (user == null) {
        user = new User(event.user.id);
    }
    Object.assign(user, event.user);
});