import './Document.js';

Board.inviteButton = document.getElementById('inviteButton');
//invitebutton class
inviteButton.addEventListener('click', (event) => {
    Board.socket.sendOpen();
});