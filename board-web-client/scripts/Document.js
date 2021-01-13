import SidebarItem from './SidebarItem.js'

export function init() {
    var next = 0;
    function newDocument() {
        new Document('Untitled' + next++);
    }
    
    document.getElementById('add').addEventListener('click', newDocument);
    
    var invite = document.location.href.substring(document.location.href.lastIndexOf("/") + 1);
    if (invite === '') {
        Board.socket.sendCreate();
    } else {
        Board.socket.sendOpen(invite);
    }
}

export default class Document {
    constructor(name) {
        this.name = name;
        this.sidebarItem = new SidebarItem(this.name, open);
    }

    open() {
        console.log(this.name + ' has been opened');
    }
}