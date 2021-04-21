import Shape from "./canvasObjects/Shape.js";
import {CanvasObjectType} from "./CanvasObjectType.js";
import CanvasImage from "./canvasObjects/CanvasImage.js";
import {activeDocument} from "../Document.js";
import CanvasObjectWrapperr from "./CanvasObjectWrapperr.js";
import socket from "../protocol/WebSocketHandler.js";
import CanvasObjectWrapper from "./CanvasObjectWrapperr.js";
import ServerMessageType from "../protocol/server/ServerMessageType.js";
import ClientMessageCanvasInsert from "../protocol/client/messages/ClientMessageCanvasInsert.js";
import ClientMessageCanvasMove from "../protocol/client/messages/ClientMessageCanvasMove.js";
import ClientMessageCanvasDelete from "../protocol/client/messages/ClientMessageCanvasDelete.js";
import CanvasDelete from "./CanvasDelete.js";
import CanvasInsert from "./CanvasInsert.js";
import CanvasMove from "./CanvasMove.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

export class Canvas {
    constructor(reader) {
        this.canvasObjects = new Map();
        this.canvasInserts = [];
        this.canvasMovesMap = new Map();
        this.canvasDeletes = [];
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
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        dt = dt / MAX_TIME * 255;
        for (let i = 0; i < this.canvasInserts.length; i++) {
            this.canvasInserts[i].dt -= dt;
            if (this.canvasInserts[i].dt <= 0) {
                this.canvasObjects.set(this.canvasInserts[i].id, this.canvasInserts[i].canvasObject);
                this.canvasInserts.splice(i--, 1);
            }
        }
        this.canvasMovesMap.forEach((canvasMoves, id) => {
            for (let i = 0; i < canvasMoves.length; i++) {
                canvasMoves[i].dt -= dt;
                if (canvasMoves[i].dt <= 0) {
                    canvasMoves.splice(i--, 1);
                }
            }
            if (canvasMoves.length > 0) {
                this.canvasMovesMap.delete(id);
            } else {
                let canvasObject = this.canvasObjects.get(id);
                if (canvasObject === undefined) {
                    console.warn('oopsie');
                    return;
                }
                canvasObject.update(canvasMoves[i].canvasObject);
            }
        });
        for (let i = 0; i < this.canvasDeletes.length; i++) {
            this.canvasDeletes[i].dt -= dt;
            if (this.canvasDeletes[i].dt <= 0) {
                this.canvasObjects.delete(this.canvasDeletes[i].id);
                this.canvasObjects.splice(i--, 1);
            }
        }
        //console.log('draw1', this.canvasObjects);
        this.canvasObjects.forEach((canvasObjectsMap, type) => {
            canvasObjectsMap.forEach((canvasObject, id) => {
                ctx.save();
                ctx.translate(canvasObject.x, canvasObject.y);
                ctx.rotate((canvasObject.rotation / 255) * (2 * Math.PI));
                canvasObject.draw();
                if (!mouse.drag && selected.canvasObject === null) {
                    if (aabb(canvasObject, mouse, SELECT_PADDING)) {
                        selected.type = type;
                        selected.id = id;
                        selected.canvasObject = canvasObject;
                    }
                }
                if (canvasObject === selected.canvasObject) {
                    ctx.strokeRect(0 - SELECT_PADDING / 2, 0 - SELECT_PADDING / 2, canvasObject.width + SELECT_PADDING, canvasObject.height + SELECT_PADDING);
                }
                ctx.restore();

            });
        });
        if (selected.canvasObject !== null && !aabb(selected.canvasObject, mouse, SELECT_PADDING)) {
            selected.canvasObject = null;
        }
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
    updateInsert(canvasInsertsMap) {
        canvasInsertsMap.forEach((canvasInserts, canvasObjectType) => {
            canvasInserts.forEach((canvasInsert) => {
                this.canvasInserts.push(new CanvasObjectWrapper(canvasObjectType, canvasInsert));
            });
        });
    }

    updateMove(canvasMovesMap) {
        canvasMovesMap.forEach((value, id) => {
            let canvasMoves = this.canvasMovesMap.get(id);
            if (canvasMoves === undefined) {
                this.canvasMovesMap.set(id, value);
            } else {
                value.forEach(canvasMove => {
                   canvasMoves.push(canvasMove);
                });
            }
        });
    }

    updateDelete(canvasDeletes) {
        //todo check if length 0 then just reassign
        canvasDeletes.forEach((canvasDelete) => {
            this.canvasDeletes.push(canvasDelete);
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
        case CanvasObjectType.IMAGE:
            object = new CanvasImage(reader);
            break;
        default:
            console.error('unknown type ' + type);
    }
    return object;
}

function aabb(rect1, rect2, padding) {
    let p = padding;
    return rect1.x - p < rect2.x + rect2.width &&
        rect1.x + rect1.width + p > rect2.x &&
        rect1.y - p < rect2.y + rect2.height &&
        rect1.y + rect1.height + p > rect2.y;
}





class UpdateCanvas {
    constructor() {
        this.canvasInsertsMap = new Map();
        this.canvasMovesMap = new Map();
        this.canvasDeletes = [];
    }

    insert(canvasObjectType, id, canvasObject) {
        let canvasInserts = this.canvasInsertsMap.get(canvasObjectType);
        if (canvasInserts === undefined) {
            canvasInserts = [];
            this.canvasInsertsMap.set(canvasObjectType, canvasInserts);
        }
        canvasInserts.push(CanvasInsert.create(getDt(), id, canvasObject));//todo
    }

    move(id, canvasObject) {
        let canvasMoves = this.canvasMovesMap.get(id);
        if (canvasMoves === undefined) {
            canvasMoves = [];
            this.canvasMovesMap.set(id, canvasMoves);
        }
        canvasMoves.push(CanvasMove.create(getDt(), canvasObject));
    }

    delete(id) {
        this.canvasDeletes.push(CanvasDelete.create(getDt(), id))
    }

    clear() {
        this.canvasInsertsMap.clear();
        this.canvasMovesMap.clear();
        this.canvasDeletes.length = 0;
    }
}

let updateCanvas = new UpdateCanvas();

const MAX_TIME = 2000;//todo copied from document.js
const SELECT_PADDING = 10;

const mouse = {
    x:0,
    y:0,
    dx:0,
    dy:0,
    width:0,
    height:0,
    down:false,
    drag:false
};

let selected = {
    canvasObjectType:0,
    id:0,
    canvasObject:null,
    dirty:false,
};

canvas.addEventListener('mousemove', (event) => {
    mouse.x = event.offsetX;
    mouse.y = event.offsetY;
    mouse.dx += event.movementX;
    mouse.dy += event.movementY;
    if (Math.max(mouse.dx, mouse.dy) > 10) {
        mouse.dx = 0;
        mouse.dy = 0;
        flushActive();
    }
    if (mouse.down) {
        mouse.drag = true;
    }
    if (mouse.drag) {
        if (selected.canvasObject !== null) {
            selected.canvasObject.x += event.movementX;
            selected.canvasObject.y += event.movementY;
            selected.dirty = true;
        } else {
            ondrag(event);
        }
    }
});

canvas.addEventListener('mousedown', (event) => {
    mouse.down = true;
});

canvas.addEventListener('mouseup', (event) => {
    mouse.down = false;
    mouse.drag = false;
});

canvas.addEventListener('mouseleave', (event) => {
    mouse.down = false;
    mouse.drag = false;
});

canvas.addEventListener('mouseenter', (event) => {
    if ((event.buttons & 1) === 1) {
        mouse.down = true;
    }
});

canvas.addEventListener('click', (event) => {
    if (selected.canvasObject === null) {
        let shape = Shape.create(event.offsetX, event.offsetY, 50, 50);
        insert(CanvasObjectType.SHAPE, shape);
    }
});

function getDt() {
    let a = (window.performance.now() - lastUpdate) / MAX_TIME * 255;
    console.log('ENCODE' + (window.performance.now() - lastUpdate) + ', ' + a);
    return a;
}

export function insert(canvasObjectType, canvasObject) {
    let id = (Math.random() - 0.5) * 32000;
    activeDocument.canvas.insert(canvasObjectType, id, canvasObject);
    updateCanvas.insert(canvasObjectType, id, canvasObject);
}

function flushActive() {
    if (activeDocument == null) {
        return;
    }
    if (selected.canvasObject !== null) {
        if (selected.dirty) {
            selected.dirty = false;
            updateCanvas.move(selected.id, selected.canvasObject);
        }
    }
}

const UPDATE_INTERVAL = 1000;
let lastUpdate = 0;

setInterval(localUpdate, UPDATE_INTERVAL);
export function localUpdate() {
    flushActive();
    lastUpdate = window.performance.now();
    //todo client side message aggregation/queue and server side understanding
    if (updateCanvas.canvasInsertsMap.size > 0) {
        socket.send(new ClientMessageCanvasInsert(updateCanvas.canvasInsertsMap));
    }
    if (updateCanvas.canvasMovesMap.size > 0) {
        socket.send(new ClientMessageCanvasMove(updateCanvas.canvasMovesMap));
    }
    if (updateCanvas.canvasDeletes.length > 0) {
        socket.send(new ClientMessageCanvasDelete(updateCanvas.canvasDeletes));
    }
    //todo flush
    updateCanvas.clear();
}

socket.addMessageListener(ServerMessageType.CANVAS_INSERT, (serverMessageCanvasInsert) => {
    if (activeDocument !== null) {
        activeDocument.canvas.updateInsert(serverMessageCanvasInsert.canvasInsertsMap);
    }
});
socket.addMessageListener(ServerMessageType.CANVAS_MOVE, (serverMessageCanvasMove) => {
   if (activeDocument !== null) {
       activeDocument.canvas.updateMove(serverMessageCanvasMove.canvasMovesMap);
   }
});
socket.addMessageListener(ServerMessageType.CANVAS_DELETE, (serverMessageCanvasDelete) => {
    if (activeDocument !== null) {
        activeDocument.canvas.updateDelete(serverMessageCanvasDelete.canvasDeletes);
    }
})

function ondrag(event) {
    //todo draw line
}

