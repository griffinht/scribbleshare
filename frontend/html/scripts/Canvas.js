export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');


export class Canvas {
    constructor() {
    }

    draw(dt) {
        if (dt < 0) {//instant draw

        }
        //todo
        this.points.forEach((points, id) => {
            ctx.beginPath();
            points.forEach((point) => {
                if (point.dt === 0) {
                    ctx.stroke();//todo only do this at the end
                    ctx.moveTo(point.x, point.y);
                } else {
                    ctx.lineTo(point.x, point.y);
                }
            })
            ctx.stroke();
        });
        /*client draw
        ctx.beginPath();
        ctx.moveTo(this.x, this.y);
        while (this.points.length > 0 && dt > 0) {
            //console.log(this.points.length);
            let point = this.points[0];
            if (point.dt === 255) {
                this.x = point.x;
                this.y = point.y;
                ctx.lineTo(this.x, this.y);
                this.points.splice(0, 1);

                continue;
            }
            if (point.dt === 0) {
                ctx.stroke();//todo only do this at the end
                this.x = point.x;
                this.y = point.y;
                ctx.moveTo(this.x, this.y);
                this.points.splice(0, 1);
                continue;
            }

            if (dt + point.usedDt < point.dt) {
                let multiplier = (dt + point.usedDt) / point.dt;
                ctx.lineTo(lerp(this.x, point.x, multiplier), lerp(this.y, point.y, multiplier));

                point.usedDt += dt;
                dt = 0;
            } else {
                this.x = point.x;
                this.y = point.y;
                ctx.lineTo(this.x, this.y);

                dt -= point.dt + point.usedDt;

                this.points.splice(0, 1);
            }
        }
        ctx.stroke();
         */
    }

    clear() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);//todo a loading screen?
    }
}

function lerp(v0, v1, t) {
    return v0 * (1 - t) + v1 * t;
}