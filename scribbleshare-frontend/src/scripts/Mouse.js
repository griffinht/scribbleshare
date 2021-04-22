import {canvas} from "./canvas/Canvas.js";

export const mouse = {
    x:0,
    y:0,
    dx:0,
    dy:0,
    width:0,
    height:0,
    down:false,
    drag:false
};

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