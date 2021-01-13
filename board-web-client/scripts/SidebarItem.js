const items = new Map();
const sidebar = document.getElementById('sidebar');

export default class SidebarItem {
    constructor(display, onclick) {
        this.button = document.createElement("button");
        let inner = document.createTextNode(doc.name);
        button.appendChild(inner);
        button.addEventListener('click', event => {
            this.setActive(event.target);
            onclick();
        });
        sidebar.appendChild(button);
        items.set(display, this);
        if (sidebar.size == 1) {
            this.setActive();
            onclick();
        }
    }

    setActive() {
        this.items.forEach(item => {
            if (this.button.innerHTML === item.innerHTML) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });
    }
}