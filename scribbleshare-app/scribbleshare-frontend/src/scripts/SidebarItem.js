const items = [];
const sidebar = document.getElementById('side');

export default class SidebarItem {
    constructor(display, open) {
        this.open = open;
        this.button = document.createElement("button");
        let inner = document.createTextNode(display);
        this.button.appendChild(inner);
        this.button.addEventListener('click', () => {
            this.setActive(true);
        });
        sidebar.appendChild(this.button);
        items.push(this);
    }

    setActive(open) {
        items.forEach(item => {
            if (this === item) {
                this.button.classList.add('active');
                if (open) {
                    this.open();
                }
            } else {
                item.button.classList.remove('active');
            }
        });
    }

    remove() {
        for (let i = 0; i < items.length; i++) {
            if (items[i] === this) {
                items.splice(i, 1);
                sidebar.removeChild(this.button);
                break;
            }
        }
    }
}