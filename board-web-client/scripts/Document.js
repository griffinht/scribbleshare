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

const documents = new Map();

export default class Document {
    constructor(name, id) {
        this.name = name;
        this.sidebarItem = new SidebarItem(this.name, open);
        if (id != null) {
            this.id = id;
            documents.set(this.id, this);
        }
    }

    open() {
        console.log(this.name + ' has been opened');
    }
}