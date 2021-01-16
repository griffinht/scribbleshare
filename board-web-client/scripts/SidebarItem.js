const items = new Map();
const sidebar = document.getElementById('side');

export default class SidebarItem {
    constructor(display, open) {
        /*
        if (items.has(display)) {
            this.button.addEventListener('click', event => {
                this.setActive(event.target);
                onclick();
            })
            return;
        }*/
        this.open = open;
        this.button = document.createElement("button");
        let inner = document.createTextNode(display);
        this.button.appendChild(inner);
        this.button.addEventListener('click', event => {
            this.setActive(event.target);
        });
        sidebar.appendChild(this.button);
        items.set(display, this);
        //todo this.setActive();
    }

    setActive() {
        items.forEach(item => {
            if (this.button.innerHTML === item.button.innerHTML) {
                item.button.classList.add('active');
                this.open();
            } else {
                item.button.classList.remove('active');
            }
        });
    }
}