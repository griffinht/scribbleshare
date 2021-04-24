import socket from "./protocol/WebSocketHandler.js";
import ClientMessageGetInvite from "./protocol/client/messages/ClientMessageGetInvite.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";

export function getInvite() {
    let invite;
    let index = document.location.href.lastIndexOf('invite=');
    if (index === -1) {
        invite = '';
    } else {
        invite = document.location.href.substring(index + 7, index + 7 + 6);
    }
    return invite;
}

const inviteButton = document.getElementById("inviteButton");

inviteButton.addEventListener('click', (event) => {
    socket.send(new ClientMessageGetInvite());
})

socket.addMessageListener(ServerMessageType.GET_INVITE, (serverMessageGetInvite) => {
    window.alert('Join at localhost/?invite=' + serverMessageGetInvite.code);
})
