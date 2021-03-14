import Client from './Client.js'
import {canvas, ctx} from "./canvas/Canvas.js";

export default class LocalClient extends Client {
    constructor() {
        super(-1);
        this.point = null;
        this.lastTime = 0;
        this.lastSend = 0;
        this.lastDirection = 0;
        this.lastX = 0;
        this.lastY = 0;
        this.refresh = true;
        this.points = [];
        console.log('drawing');
        canvas.addEventListener('mousedown', (event) => {this.mousedown(event)});
        canvas.addEventListener('mouseup', (event) => {
            document.getElementsByTagName('body')[0].classList.remove('noselect');
            if (this.point !== null) {
                this.pushPoint();
                this.point = null;
            }
        });
        canvas.addEventListener('mouseenter', (event) => {this.mousedown(event)});
        canvas.addEventListener('mousemove', (event) => {
            if (event.buttons & 1 && this.point !== null) {
                console.log('drawing');
                ctx.beginPath();
                ctx.moveTo(event.offsetX - event.movementX, event.offsetY - event.movementY);
                ctx.lineTo(event.offsetX, event.offsetY);
                ctx.stroke();
                let now = performance.now();
                this.point.dt += now - this.lastTime;
                this.point.x = event.offsetX;
                this.point.y = event.offsetY;
                if (this.point.dt > 10 && (Math.abs(Math.atan2(this.point.y - this.lastX, this.point.x - this.lastY) - this.lastDirection) > 0.1 || this.point.dt > UPDATE_INTERVAL)) {
                    this.pushPoint();
                    this.point = {
                        dt:0,
                        x:this.point.x,
                        y:this.point.y,
                    };
                }
                this.lastTime = now;
            }
        });

    }

    mousedown(event) {
        document.getElementsByTagName('body')[0].classList.add('noselect');
        if (event.buttons & 1) {
            console.log('sdf');
            this.lastTime = performance.now();
            this.points.push({
                dt:0,
                x:event.offsetX,
                y:event.offsetY,
            })
            this.point = {
                dt:this.lastTime - this.lastSend,
                x:event.offsetX,
                y:event.offsetY,
            }
            this.lastX = event.offsetX;
            this.lastY = event.offsetY;
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
}