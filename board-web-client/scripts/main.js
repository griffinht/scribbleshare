import LocalClient from './LocalClient.js';
import WebSocketHandler from './WebSocketHandler.js'
import Sidebar from './Sidebar.js'
import Canvas from './Canvas.js'

new Sidebar();
new Canvas();

localClient = new LocalClient();
var socket = null;

inviteButton.addEventListener('click', (event) => {
    if (socket == null) {
        socket = new WebSocketHandler();
    }
});

let index = document.location.href.lastIndexOf("/");
if (document.location.href.substring(index - 2, index + 1) === '/r/') {
    socket = new WebSocketHandler();
}