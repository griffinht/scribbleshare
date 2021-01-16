const items = new Map();
const sidebar = document.getElementById('side');

export default class SidebarItem {
    constructor(display, onclick) {
        /*
        if (items.has(display)) {
            this.button.addEventListener('click', event => {
                this.setActive(event.target);
                onclick();
            })
            return;
        }*/
        this.button = document.createElement("button");
        let inner = document.createTextNode(display);
        this.button.appendChild(inner);
        this.button.addEventListener('click', event => {
            this.setActive(event.target);
            onclick();
        });
        sidebar.appendChild(this.button);
        items.set(display, this);
        if (sidebar.size == 1) {
            this.setActive();
            onclick();
        }
    }

    setActive() {
        items.forEach(item => {
            if (this.button.innerHTML === item.button.innerHTML) {
                item.button.classList.add('active');
            } else {
                item.button.classList.remove('active');
            }
        });
    }
}