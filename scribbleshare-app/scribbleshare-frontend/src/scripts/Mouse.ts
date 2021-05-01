export default class Mouse {
    x: number;
    y: number;
    dx: number;
    dy: number;
    width: number;
    height: number;
    down: boolean;
    drag: boolean;

    constructor(element: HTMLElement) {
        this.x = 0;
        this.y = 0;
        this.dx = 0;
        this.dy = 0;
        this.width = 0;
        this.height = 0;
        this.down = false;
        this.drag = false;
        element.addEventListener('mousemove', (event) => {
            this.x = event.offsetX;//todo left click drag only
            this.y = event.offsetY;
            this.dx += event.movementX;
            this.dy += event.movementY;
            if (this.down && ((event.buttons & 1) === 1 || (event.buttons & 2) === 2)) {
                this.drag = true;
            }
        });
        element.addEventListener('mousedown', () => {
            this.down = true;
        });

        element.addEventListener('mouseup', () => {
            this.down = false;
            this.drag = false;
        });

        element.addEventListener('mouseleave', () => {
            this.down = false;
            this.drag = false;
        });
        element.addEventListener('mouseenter', (event) => {
            if (((event.buttons & 1) === 1 || (event.buttons & 2) === 1)) {
                this.down = true;
            }
        });

    }

    reset() {
        this.dx = 0;
        this.dy = 0;
    }
}