import socket from './WebSocketHandler.js'

const inviteButton = document.getElementById('inviteButton');
inviteButton.addEventListener('click', () => {
    socket.sendOpen();
});
socket.addEventListener('open', () => {
    inviteButton.innerHTML = 'invite';
})
