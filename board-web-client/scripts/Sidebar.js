export default class Sidebar {
    constructor() {
        this.sidebar = document.getElementById('side');
        this.sidebarButtons = [];
        this.untitled = 0;
    }

    createButton(name, active) {
        let button = document.createElement("button");
        let inner = document.createTextNode(name);
        button.appendChild(inner);
        button.addEventListener('click', event => this.setActive(event.target));
        this.sidebar.appendChild(button);
        this.sidebarButtons.push(button);
        if (active) {
            this.setActive(button);
        }
    }

    setActive(target) {
        this.sidebarButtons.forEach(sidebarButton => {
            if (target.innerHTML === sidebarButton.innerHTML) {
                sidebarButton.classList.add('active');
            } else {
                sidebarButton.classList.remove('active');
            }
        });
    }
}