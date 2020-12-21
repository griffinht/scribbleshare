import Client from './Client.js'
export default class LocalClient extends Client {
    constructor(id) {
        super(-1);
        this.point = null;
        this.lastTime = 0;
        this.lastSend = 0;
        this.lastDirection = 0;
        this.refresh = true;
        this.points = [];
        console.log(this.point);
        canvas.addEventListener('mousedown', (event) => {this.mousedown(event)});
        canvas.addEventListener('mouseup', (event) => {
            if (this.point !== null) {
                this.pushPoint();
                this.point = null;
            }
        });
        canvas.addEventListener('mouseenter', (event) => {this.mousedown(event)});
        canvas.addEventListener('mousemove', (event) => {
            if (event.buttons & 1 && this.point !== null) {
                ctx.beginPath();
                ctx.moveTo(event.x - event.movementX, event.y - event.movementY);
                ctx.lineTo(event.x, event.y);
                ctx.stroke();
                let now = performance.now();
                this.point.dt += now - this.lastTime;
                this.point.x += event.movementX;
                this.point.y += event.movementY;
                if (this.point.dt > 10 && (Math.abs(Math.atan2(this.point.y, this.point.x) - this.lastDirection) > 0.1 || this.point.dt > 100)) {
                    this.pushPoint();
                    this.point = {
                        dt:0,
                        x:0,
                        y:0,
                    };
                }
                this.lastTime = now;
            }
        });
    }

    draw(dt) {

    }

    mousedown(event) {
        if (event.buttons & 1) {
            this.lastTime = performance.now();
            this.points.push({
                dt:0,
                x:event.x,
                y:event.y,
            })
            this.point = {
                dt:this.lastTime - this.lastSend,
                x:0,
                y:0,
            }
        }
    }

    pushPoint() {
        if (this.point.dt !== 0 ||
            this.point.x !== 0 ||
            this.point.y !== 0) {
            this.points.push(this.point);
            this.lastDirection = Math.atan2(this.point.y, this.point.x);
        }
    }

    getPoints() {
        this.lastSend = performance.now();

        this.points.forEach(p => console.log(p));
        
        return this.points;
    }
}