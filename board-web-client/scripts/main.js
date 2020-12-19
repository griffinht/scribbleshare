import WebSocketHandler from './WebSocketHandler.js'

var canvas = document.getElementById('canvas');
var ctx = canvas.getContext('2d');

// handle resize
window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        //todo redraw?
};
resizeCanvas();

// handle mouse interactions
var mouse = {
    x:0,
    y:0,
    isDown:false,
}
canvas.addEventListener('mousedown', (event) => {
    mouse.isDown = true;
});
canvas.addEventListener('mouseup', (event) => {
    mouse.isDown = false;
    mouse.wasDown = false;
});
canvas.addEventListener('mousemove', (event) => {
    if (mouse.isDown) {
        ctx.beginPath();
        ctx.moveTo(event.x - event.movementX, event.y - event.movementY);
        ctx.lineTo(event.x, event.y);
        ctx.stroke();
        mouse.x = event.x;
        mouse.y = event.y;
    }
});

const socket = new WebSocketHandler();
socket.send('poggers');


