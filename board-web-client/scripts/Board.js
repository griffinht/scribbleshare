import LocalClient from './LocalClient.js';
import WebSocketHandler from './WebSocketHandler.js'
import Canvas from './Canvas.js'
import Document from './Document.js';
import {init} from './Document.js';

Board.clients = new Map();
Board.inviteButton = document.getElementById('inviteButton');

Board.localClient = new LocalClient();
Board.socket = new WebSocketHandler();

new Canvas();
Board.socket.addEventListener('open', () => {
    init();
});

inviteButton.addEventListener('click', (event) => {
    Board.socket.sendOpen();
});