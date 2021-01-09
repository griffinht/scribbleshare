import LocalClient from './LocalClient.js';
import WebSocketHandler from './WebSocketHandler.js'
import Sidebar from './Sidebar.js'
import Canvas from './Canvas.js'

new Sidebar();
new Canvas();

localClient = new LocalClient();
var socket = new WebSocketHandler();

inviteButton.addEventListener('click', (event) => {
    socket.sendOpen();
});

let index = document.location.href.lastIndexOf("/");
if (document.location.href.substring(index - 2, index + 1) === '/r/') {
    socket = new WebSocketHandler();
}