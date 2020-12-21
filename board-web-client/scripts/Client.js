export default class Client {
    constructor(id) {
        this.id = id;
        this.x = 0;
        this.y = 0;
        this.points = [];
        clients.set(this.id, this);
    }

    draw(dt) {
        ctx.beginPath();
        ctx.moveTo(this.x, this.y);
        while (this.points.length > 0 && dt > 0) {
            //console.log(this.points.length);
            let point = this.points[0];
            if (point.dt === 0) {
                ctx.stroke();//todo only do this at the end
                this.x = point.x;
                this.y = point.y;
                ctx.moveTo(this.x, this.y);
                this.points.splice(0, 1);
                continue;
            }
            let multiplier;
            
            if (dt + point.usedDt < point.dt) {
                multiplier = (dt + point.usedDt) / point.dt;
                ctx.lineTo(this.x + lerp(0, point.x, multiplier), this.y + lerp(0, point.y, multiplier));

                point.usedDt += dt;
                dt = 0;
            } else {
                multiplier = 1;
                ctx.lineTo(this.x + lerp(0, point.x, multiplier), this.y + lerp(0, point.y, multiplier));

                dt -= point.dt + point.usedDt;
                this.x += point.x;
                this.y += point.y;
                this.points.splice(0, 1);
            }
        }
        ctx.stroke();
    }
}

function lerp(v0, v1, t) {
    return v0 * (1 - t) + v1 * t;
}