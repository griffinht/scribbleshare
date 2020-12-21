import LocalClient from './LocalClient.js';
import WebSocketHandler from './WebSocketHandler.js'

// handle resize
window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
        let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        canvas.width = canvas.parentElement.offsetWidth;
        canvas.height = canvas.parentElement.offsetHeight;
        ctx.putImageData(imageData, 0, 0);
        //todo redraw?
};
resizeCanvas();

const socket = new WebSocketHandler();
localClient = new LocalClient();

let last = performance.now();
function draw(now) {
    let dt = (now - last);
    last = now;

    clients.forEach(e => e.draw(dt));

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);