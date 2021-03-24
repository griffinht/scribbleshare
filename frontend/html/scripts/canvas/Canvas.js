import Shape from "./canvasObjects/Shape.js";
import {CanvasObjectType} from "./CanvasObjectType.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');


export class Canvas {
    constructor(reader) {
        this.canvasObjects = new Map();
        if (reader != null) {
            for (let i = 0; i < reader.readUint8(); i++) {
                let type = reader.readUint8();
                let map = new Map();
                this.canvasObjects.set(type, map);
                for (let j = 0; j < reader.readUint16(); j++) {
                    map.set(reader.readInt16(), getCanvasObject(type, reader));
                }
            }
        }
    }

    draw(dt) {
        //todo
        /*this.objects.forEach((type, objects) => {
            objects.forEach((object) => {
                object.draw(dt);
            })*/
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
        //});
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

    update(updateCanvasObjects) {
        updateCanvasObjects.forEach((value, key) => {
            let map = this.canvasObjects.get(key);
            if (map == null) {
                this.canvasObjects.set(key, value);
            } else {
                value.forEach((v, k) => {
                    map.set(k, v);
                });
            }
        });
    }

    delete(deleteCanvasObjects) {
        deleteCanvasObjects.forEach((value, key) => {
            let map = this.canvasObjects.get(key);
            if (map == null) {
                console.warn('cant delete ' + key + ' its already gone');
            } else {
                value.forEach((v, k) => {
                    map.remove(k);
                });
            }
        });
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
}

function lerp(v0, v1, t) {
    return v0 * (1 - t) + v1 * t;
}


function getCanvasObject(type, reader) {
    let object;
    switch (type) {
        case CanvasObjectType.SHAPE:
            object = new Shape(reader);
        default:
            console.error('unknown type ' + type);
    }
    return object;
}