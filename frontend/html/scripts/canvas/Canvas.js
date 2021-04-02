import Shape from "./canvasObjects/Shape.js";
import {CanvasObjectType} from "./CanvasObjectType.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

const MAX_TIME = 2000;//todo copied from document.js

export class Canvas {
    constructor(reader) {
        this.canvasObjects = new Map();
        this.updateCanvasObjects = new Map();
        if (reader != null) {
            let length = reader.readUint8();
            for (let i = 0; i < length; i++) {
                let type = reader.readUint8();
                let map = new Map();
                this.canvasObjects.set(type, map);
                let lengthJ = reader.readUint16();
                for (let j = 0; j < lengthJ; j++) {
                    map.set(reader.readInt16(), getCanvasObject(type, reader));
                }
            }
        }
    }

    draw(dt) {
        dt = dt / 2000 * 255;
        this.updateCanvasObjects.forEach((value, key) => {
            let map = this.canvasObjects.get(key);
            if (map == null) {
                map = new Map();
                this.canvasObjects.set(key, map);
            }
            value.forEach((v, key) => {
                v.dt -= dt;
                if (v.dt <= 0) {
                    map.set(key, v.canvasObject);
                    value.delete(key);//is this bad? (concurrent modification???)
                }
            })
        });
        //console.log('draw1', this.canvasObjects);
        this.canvasObjects.forEach((value, key) => {
            value.forEach((v, k) => {
                ctx.save();
                ctx.translate(value.x, value.y);
                v.draw();
                ctx.restore();
            })
        })
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

    //for remote drawing, places into queue for being updated
    updateMultiple(updateCanvasObjectWrappers) {
        console.log(this.updateCanvasObjects);
        updateCanvasObjectWrappers.forEach((value, key) => {
            let map = this.updateCanvasObjects.get(key);
            if (map == null) {
                map = new Map();
                this.updateCanvasObjects.set(key, map);
            }
            value.forEach((v, k) => {
                map.set(k, v);
            });
        });
    }

    update(canvasObjectType, id, canvasObjectWrapper) {
        let map = this.updateCanvasObjects.get(canvasObjectType);
        if (map == null) {
            map = new Map();
            this.updateCanvasObjects.set(canvasObjectType, map);
        }
        map.set(id, canvasObjectWrapper);
    }

    //for local drawing, updates canvas instantly
    insert(type, id, canvasObject) {
        let map = this.canvasObjects.get(type);
        if (map == null) {
            map = new Map();
            this.canvasObjects.set(type, map);
        }
        map.set(id, canvasObject);//todo random id is bad
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

    resize() {

        //todo redraw?
    }
}

function lerp(v0, v1, t) {
    return v0 * (1 - t) + v1 * t;
}


export function getCanvasObject(type, reader) {
    let object;
    switch (type) {
        case CanvasObjectType.SHAPE:
            object = new Shape(reader);
            break;
        default:
            console.error('unknown type ' + type);
    }
    return object;
}