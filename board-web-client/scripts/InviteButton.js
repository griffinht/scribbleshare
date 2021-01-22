import socket from './WebSocketHandler.js'

const inviteButton = document.getElementById('inviteButton');
inviteButton.addEventListener('click', () => {
    socket.sendOpen();
});
socket.addEventListener('socket.open', () => {//todo protocol open or document open?
    inviteButton.innerHTML = 'invite';
})
