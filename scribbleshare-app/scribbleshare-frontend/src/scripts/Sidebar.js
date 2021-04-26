export default class Sidebar {
    constructor(element) {
        this.items = [];
        this.element = element;
    }

    addItem(display, open) {
        let a = new SidebarItem(display, open, this);
        this.items.push(a);
        return a;
    }
}

class SidebarItem {
    constructor(display, open, a) {
        this.a = a;
        this.open = open;
        this.button = document.createElement("button");
        let inner = document.createTextNode(display);
        this.button.appendChild(inner);
        this.button.addEventListener('click', () => {
            this.setActive(true);
        });
        this.a.element.appendChild(this.button);
    }

    setActive(open) {
        this.a.items.forEach(item => {
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
        for (let i = 0; i < this.a.items.length; i++) {
            if (this.a.items[i] === this) {
                this.a.items.splice(i, 1);
                this.a.element.removeChild(this.button);
                break;
            }
        }
    }
}