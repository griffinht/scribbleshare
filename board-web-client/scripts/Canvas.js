export default class Canvas {// handle resize
    constructor() {
        window.addEventListener('resize', resizeCanvas);
        function resizeCanvas() {
                let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
                canvas.width = canvas.parentElement.offsetWidth;
                canvas.height = canvas.parentElement.offsetHeight;
                ctx.putImageData(imageData, 0, 0);
                //todo redraw?
        };
        resizeCanvas();
    
        let last = performance.now();
        function draw(now) {
            let dt = (now - last);
            last = now;
    
            clients.forEach(e => e.draw(dt));
    
            window.requestAnimationFrame(draw);
        }
        window.requestAnimationFrame(draw);
    }
}