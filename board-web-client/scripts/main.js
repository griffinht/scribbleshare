import LocalClient from './LocalClient.js';
import WebSocketHandler from './WebSocketHandler.js'
import Sidebar from './Sidebar.js'

new Sidebar();
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

localClient = new LocalClient();
var socket = null;

let last = performance.now();
function draw(now) {
    let dt = (now - last);
    last = now;

    clients.forEach(e => e.draw(dt));

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);

inviteButton.addEventListener('click', (event) => {
    if (socket == null) {
        socket = new WebSocketHandler();
    }
});

let index = document.location.href.lastIndexOf("/");
if (document.location.href.substring(index - 2, index + 1) === '/r/') {
    socket = new WebSocketHandler();
}