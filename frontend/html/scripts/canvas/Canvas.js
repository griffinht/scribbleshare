import {CanvasObjectType} from "./CanvasObjectType.js";
import Points from "./canvasObjects/Points.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');


export class Canvas {
    constructor(reader) {
        this.objects = new Map();
        this.updatedObjects = new Map();
        if (reader != null) {
            for (let i = 0; i < reader.readUint8(); i++) {
                let type = reader.readUint8();
                if (this.objects.get(CanvasObjectType.POINTS) == null) {
                    this.objects.set(CanvasObjectType.POINTS, []);
                }
                switch (type) {
                    case CanvasObjectType.POINTS:
                        let objects = this.objects.get(CanvasObjectType.POINTS);
                        for (let j = 0; j < reader.readUint8(); j++) {
                            objects.push(new Points(reader));
                        }
                }
            }
        }
    }

    draw(dt) {
        //todo
        this.objects.forEach((type, objects) => {
            objects.forEach((object) => {
                object.draw(dt);
            })
            /*ctx.beginPath();
            points.forEach((point) => {
                if (point.dt === 0) {
                    ctx.stroke();//todo only do this at the end
                    ctx.moveTo(point.x, point.y);
                } else {
                    ctx.lineTo(point.x, point.y);
                }
            })
            ctx.stroke();*/
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
        //todo
        /*let e = {};
        e.id = reader.readInt16();
        let size = reader.readUint16();
        e.points = [];
        for (let i = 0; i < size; i++) {
            let point = {};
            point.dt = reader.readUint8();
            point.x = reader.readInt16();
            point.y = reader.readInt16();
            point.usedDt = 0;
            e.points.push(point);
        }
        this.dispatchEvent('protocol.updateDocument', e);*/
    }

    update(canvas) {

    }

    updateObject(object) {

    }

    clear() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);//todo a loading screen?
    }

    resize() {
        let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        let rect = canvas.parentNode.getBoundingClientRect();
        canvas.width = rect.width;
        canvas.height = rect.height;
        ctx.putImageData(imageData, 0, 0);
        //todo redraw?
    }

    serialize(writer) {
        writer.writeUint8(this.updatedObjects.size);
        this.updatedObjects.forEach((type, objects) => {
            writer.writeUint8(type);
            writer.writeUint16(objects.size());
            objects.forEach((object) => {
                object.serialize(writer);
            })
        })
        this.objects.forEach((type, objects) => {

        })
        this.updatedObjects.clear();
    }
}

function lerp(v0, v1, t) {
    return v0 * (1 - t) + v1 * t;
}