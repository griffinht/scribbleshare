import socket from "./protocol/WebSocketHandler.js";
import ClientMessageGetInvite from "./protocol/client/messages/ClientMessageGetInvite.js";
import ServerMessageType from "./protocol/server/ServerMessageType.js";
import Modal from "./Modal.js";
import ServerMessageGetInvite from "./protocol/server/messages/ServerMessageGetInvite.js";
import ServerMessage from "./protocol/server/ServerMessage.js";

const inviteButton = document.getElementById("inviteButton")!;
const inviteModal = new Modal(document.getElementById('inviteModal')!);
const text = inviteModal.modal.getElementsByTagName("p")[0];

class Invite {
    visible: boolean = true;

    constructor() {
        inviteButton.addEventListener('click', (event) => {
            socket.send(new ClientMessageGetInvite());
        })
        socket.addMessageListener(ServerMessageType.GET_INVITE, (serverMessage: ServerMessage) => {
            let serverMessageGetInvite = serverMessage as ServerMessageGetInvite;
            let link = 'http://localhost/?invite=' + serverMessageGetInvite.code;
            text.innerHTML = 'Join at <a href="' + link + '">' + link + "</a>";
            inviteModal.show();
        })
        this.setVisible(false);
    }

    getInvite() {
        let invite;
        let index = document.location.href.lastIndexOf('invite=');
        if (index === -1) {
            invite = '';
        } else {
            invite = document.location.href.substring(index + 7, index + 7 + 6);
        }
        return invite;
    }

    setVisible(visible: boolean) {
        if (visible) {
            inviteButton.style.visibility = 'visible';
        } else {
            inviteButton.style.visibility = 'hidden';
        }
    }

}
const invite = new Invite();
export default invite;


document.getElementById('inviteModalClose')!.addEventListener('click', (event) => {
    inviteModal.hide();
})
