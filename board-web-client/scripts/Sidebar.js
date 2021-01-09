export default class Sidebar {
    constructor() {
        this.sidebar = document.getElementById('side');
        this.sidebarButtons = [];
        this.untitled = 0;
        this.createButton().classList.add('active');
        document.getElementById('add').addEventListener('click', event => {
            this.createButton();
        })
    }

    createButton(name) {
        if (name == null) {
            name = 'Untitled ' + ++this.untitled;
        }
        let button = document.createElement("button");
        let inner = document.createTextNode(name);
        button.appendChild(inner);
        button.addEventListener('click', event => {
            this.sidebarButtons.forEach(sidebarButton => {
                if (event.target.innerHTML === sidebarButton.innerHTML) {
                    sidebarButton.classList.add('active');
                } else {
                    sidebarButton.classList.remove('active');
                }
            });
        });
        this.sidebar.appendChild(button);
        this.sidebarButtons.push(button);
        return button;
    }
}