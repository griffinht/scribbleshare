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
Board.socket.addEventListener('socketopen', () => {
    init();
});
Board.socket.addEventListener('addclient', (event) => {
    console.log('Add client ', new Client(event.id));
})
Board.socket.addEventListener('removeclient', (event) => {
    console.log('Remove client ', Board.clients.delete(event.id));
})
Board.socket.addEventListener('draw', (event) => {
    let client = Board.clients.get(event.id);
    event.points.forEach(point => {//todo styling (point) => {} or point => {}
        client.points.push(point);
    });
})
Board.socket.addEventListener('open', (event) => {
    let doc = new Document(event.id, event.name);
    //window.history.pushState(doc.name, document.title, '/d/' + id);
    Board.inviteButton.innerHTML = 'invite';//todo abstract invitebutton to class???????
    let d = Board.sidebar.sidebarButtons.get(doc.id);
    if (d != null) {
        Board.sidebar.setActive(d);
    } else {
        Board.sidebar.createButton(doc, true, () => {
            this.sendOpen(id);
        });
    }
})

inviteButton.addEventListener('click', (event) => {
    Board.socket.sendOpen();
});