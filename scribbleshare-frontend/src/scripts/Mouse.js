export default class Mouse {
    constructor(element) {
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
            element.dy += event.movementY;
            if (this.down) {
                this.drag = true;
            }
        });
        element.addEventListener('mousedown', (event) => {
            this.down = true;
        });

        element.addEventListener('mouseup', (event) => {
            this.down = false;
            this.drag = false;
        });

        element.addEventListener('mouseleave', (event) => {
            this.down = false;
            this.drag = false;
        });
        element.addEventListener('mouseenter', (event) => {
            if ((event.buttons & 1) === 1) {
                this.down = true;
            }
        });

    }

    reset() {
        this.dx = 0;
        this.dy = 0;
    }
}