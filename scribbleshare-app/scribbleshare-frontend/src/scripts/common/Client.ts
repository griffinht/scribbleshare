import MouseMove from "./MouseMove.js";

export default class Client {
    id: number;
    user: bigint;
    icon: HTMLImageElement;
    iconTooltip: HTMLElement;

    mouseMoves: MouseMove[] = [];
    time = 0;
    first = true;

    constructor(id: number, user: bigint) {
        this.id = id;
        this.user = user;
        //tooltip
        this.icon = document.createElement('img');
        this.icon.setAttribute('src', 'assets/default.png');
        this.icon.addEventListener('mouseenter', (event) => {
            let rect = this.icon.getBoundingClientRect();
            this.iconTooltip.style.visibility = 'visible';
            this.iconTooltip.style.display = 'initial';
            this.iconTooltip.style.top = rect.top + 'px';
            this.iconTooltip.style.left = rect.left + -150 + 'px';
        })
        this.icon.addEventListener('mouseleave', (event) => {
            this.iconTooltip.style.visibility = 'hidden';
            this.iconTooltip.style.display = 'none';
        })
        this.iconTooltip = document.createElement('div');
        if (user != null) {
            this.iconTooltip.innerText = String(this.user);
        }
        this.iconTooltip.style.position = 'absolute';
        this.iconTooltip.style.visibility = 'hidden';
        this.iconTooltip.style.zIndex = '1000';
        this.iconTooltip.style.color = 'black';
        this.iconTooltip.style.display = 'none';
        document.body.appendChild(this.iconTooltip);
    }
}