export default class Client {
    constructor(id, user) {
        this.id = id;
        this.user = user;
        this.mouseX = 0;
        this.mouseY = 0;
        //tooltip
        this.icon = document.createElement('img');
        this.icon.setAttribute('src', 'assets/default.png');
        this.icon.addEventListener('mouseenter', (event) => {
            let rect = this.icon.getBoundingClientRect();
            this.iconTooltip.style.visibility = 'visible';
            this.iconTooltip.style.top = rect.top + 'px';
            this.iconTooltip.style.left = rect.left + -50 + 'px';
        })
        this.icon.addEventListener('mouseleave', (event) => {
            this.iconTooltip.style.visibility = 'hidden';
        })
        this.iconTooltip = document.createElement('div');
        if (user != null) {
            this.iconTooltip.innerText = user.id;
        }
        this.iconTooltip.style.position = 'absolute';
        this.iconTooltip.style.visibility = 'hidden';
        this.iconTooltip.style.zIndex = '1000';
        this.iconTooltip.style.color = 'black';
        document.getElementsByTagName('body')[0].parentNode.appendChild(this.iconTooltip);
    }
}