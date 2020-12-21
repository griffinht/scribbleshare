export default class Client {
    constructor(id) {
        this.id = id;
        this.x = 0;
        this.y = 0;
        this.points = [];
        clients.set(this.id, this);
    }

    draw(dt) {
        let now = performance.now();
        let i;
        ctx.beginPath();
        ctx.moveTo(this.x, this.y);
        if (this.points.length > 0) {
            console.log(this.points.length);
        }
        for (i = 0; i < this.points.length; i++) {
            //console.log(this.points.length);
            let point = this.points[i];
            if (point.dt === 0) {
                ctx.stroke();//todo only do this at the end
                this.x = point.x;
                this.y = point.y;
                ctx.moveTo(this.x, this.y);
                continue;
            }
            this.x += point.x;
            this.y += point.y;
            ctx.lineTo(this.x, this.y);
            ///otherwise remove from the array, this point has been drawn
        }
        ctx.stroke();
        if (this.points.length > 0) {
            console.log((performance.now() - now) + 'time');
        }
        this.points.splice(0, i + 1);
        
    }
}