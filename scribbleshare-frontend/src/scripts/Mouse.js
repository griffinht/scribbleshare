import {canvas} from "./canvas/Canvas.js";

class Mouse {
    constructor() {
        this.x = 0;
        this.y = 0;
        this.dx = 0;
        this.dy = 0;
        this.width = 0;
        this.height = 0;
        this.down = false;
        this.drag = false;
    }

    reset() {
        this.dx = 0;
        this.dy = 0;
    }
}
export const mouse = new Mouse();

canvas.addEventListener('mousemove', (event) => {
    mouse.x = event.offsetX;//todo left click drag only
    mouse.y = event.offsetY;
    mouse.dx += event.movementX;
    mouse.dy += event.movementY;
    if (mouse.down) {
        mouse.drag = true;
    }
});

canvas.addEventListener('mousedown', (event) => {
    mouse.down = true;
});

canvas.addEventListener('mouseup', (event) => {
    mouse.down = false;
    mouse.drag = false;
});

canvas.addEventListener('mouseleave', (event) => {
    mouse.down = false;
    mouse.drag = false;
});
canvas.addEventListener('mouseenter', (event) => {
    if ((event.buttons & 1) === 1) {
        mouse.down = true;
    }
});