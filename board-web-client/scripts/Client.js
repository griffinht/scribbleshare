import {clientsToolbar, ctx} from './Document.js'

export default class Client {
    constructor(id, user) {
        this.id = id;
        this.user = user;
        this.x = 0;
        this.y = 0;
        this.points = [];
        this.icon = document.createElement('img');
        this.icon.setAttribute('src', '/bin/default.png');
        this.icon.addEventListener('mouseenter', (event) => {
            let rect = this.icon.getBoundingClientRect();
            this.iconTooltip.style.visibility = 'visible';
            this.iconTooltip.style.top = rect.top + 'px';
            this.iconTooltip.style.left = rect.left + -50 + 'px';
        })
        this.icon.addEventListener('mouseleave', (event) => {
            this.iconTooltip.style.visibility = 'hidden';
        })
        this.iconTooltip = document.createElement('div');
        if (user != null) {
            this.iconTooltip.innerText = user.id;
        }
        this.iconTooltip.style.position = 'absolute';
        this.iconTooltip.style.visibility = 'hidden';
        this.iconTooltip.style.zIndex = '1000';
        this.iconTooltip.style.color = 'black';
        document.getElementsByTagName('body')[0].parentNode.appendChild(this.iconTooltip);
    }

    draw(dt) {
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
    }
}

function lerp(v0, v1, t) {
    return v0 * (1 - t) + v1 * t;
}