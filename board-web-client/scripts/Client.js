export default class Client {
    constructor(id) {
        this.id = id;
        this.x = 0;
        this.y = 0;
        this.points = [];
        clients.set(this.id, this);
    }

    draw(dt) {
        let i;
        for (i = 0; i < this.points.length; i++) {
            console.log(this.points.length);
            let point = this.points[i];
            if (point.dt === 0) {
                this.x = point.x;
                this.y = point.y;
                continue;
            }
            dt -= point.dt;
            let multiplier;
            if (dt < 0) {
                multiplier = point.dt / (dt + point.dt);
                dt = 0;
            } else {
                multiplier = 1;
            }
            ctx.beginPath();
            ctx.moveTo(this.x, this.y);
            this.x += point.x;
            this.y += point.y;
            ctx.lineTo(this.x, this.y);
            ctx.stroke();//todo only do this at the end
            if (dt > 0) { //if dt runs out before the point, then there may still be drawable points
                break;
            }
            ///otherwise remove from the array, this point has been drawn
        }
        this.points.splice(0, i + 1);
    }
}