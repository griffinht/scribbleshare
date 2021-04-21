import Shape from "./canvasObjects/Shape.js";
import {CanvasObjectType} from "./CanvasObjectType.js";
import CanvasImage from "./canvasObjects/CanvasImage.js";
import {activeDocument} from "../Document.js";
import socket from "../protocol/WebSocketHandler.js";
import CanvasObjectWrapper from "./CanvasObjectWrapper.js";
import ServerMessageType from "../protocol/server/ServerMessageType.js";
import ClientMessageCanvasInsert from "../protocol/client/messages/ClientMessageCanvasInsert.js";
import ClientMessageCanvasMove from "../protocol/client/messages/ClientMessageCanvasMove.js";
import ClientMessageCanvasDelete from "../protocol/client/messages/ClientMessageCanvasDelete.js";
import CanvasDelete from "./CanvasDelete.js";
import CanvasInsert from "./CanvasInsert.js";
import CanvasMove from "./CanvasMove.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

let selected = {
    id:0,
    canvasObjectWrapper:null,
    dirty:false,
};

export class Canvas {
    constructor(reader) {
        this.canvasObjectWrappers = new Map();
        this.canvasInserts = [];
        this.canvasMovesMap = new Map();
        this.canvasDeletes = [];
        if (reader != null) {
            let length = reader.readUint8();
            for (let i = 0; i < length; i++) {
                let type = reader.readUint8();
                let lengthJ = reader.readUint16();
                for (let j = 0; j < lengthJ; j++) {
                    this.canvasObjectWrappers.set(reader.readInt16(), new CanvasObjectWrapper(type, getCanvasObject(type, reader)));
                }
            }
        }
    }

    draw(dt) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        dt = dt / MAX_TIME * 255;
        for (let i = 0; i < this.canvasInserts.length; i++) {
            //console.log('inserts', this.canvasInserts[i].dt);
            this.canvasInserts[i].dt -= dt;
            if (this.canvasInserts[i].dt <= 0) {
                this.canvasObjectWrappers.set(this.canvasInserts[i].id, this.canvasInserts[i].canvasObjectWrapper);
                this.canvasInserts.splice(i--, 1);
            }
        }
        this.canvasMovesMap.forEach((canvasMoves, id) => {
            for (let i = 0; i < canvasMoves.length; i++) {
                //console.log('moves', canvasMoves[i].dt);
                canvasMoves[i].dt -= dt;
            }
            if (canvasMoves.length > 0) {
                if (canvasMoves[0].dt <= 0) {
                    let canvasObjectWrapper = this.canvasObjectWrappers.get(id);
                    if (canvasObjectWrapper === undefined) {
                        console.warn('oopsie');
                        return;
                    }
                    canvasObjectWrapper.canvasObject.update(canvasMoves[0].canvasObject);//todo interpolate

                    canvasMoves.splice(0, 1);
                }
                //todo interpolate
            } else {
                this.canvasMovesMap.delete(id);
            }
        });
        for (let i = 0; i < this.canvasDeletes.length; i++) {
            //console.log('deletes', this.canvasDeletes[i].dt);
            this.canvasDeletes[i].dt -= dt;
            if (this.canvasDeletes[i].dt <= 0) {
                this.canvasObjectWrappers.delete(this.canvasDeletes[i].id);
                this.canvasDeletes.splice(i--, 1);
            }
        }
        //console.log('draw1', this.canvasObjects);
        this.canvasObjectWrappers.forEach((canvasObjectWrapper, id) => {
            ctx.save();
            ctx.translate(canvasObjectWrapper.canvasObject.x, canvasObjectWrapper.canvasObject.y);
            ctx.rotate((canvasObjectWrapper.canvasObject.rotation / 255) * (2 * Math.PI));
            canvasObjectWrapper.canvasObject.draw();
            if (selected.canvasObjectWrapper === null) {
                if (!mouse.drag) {
                    if (aabb(canvasObjectWrapper.canvasObject, mouse, SELECT_PADDING)) {
                        selected.id = id;
                        selected.canvasObjectWrapper = canvasObjectWrapper;
                    }
                }
            } else {
                if (canvasObjectWrapper.canvasObject === selected.canvasObjectWrapper.canvasObject) {
                    ctx.strokeRect(0 - SELECT_PADDING / 2, 0 - SELECT_PADDING / 2, canvasObjectWrapper.canvasObject.width + SELECT_PADDING, canvasObjectWrapper.canvasObject.height + SELECT_PADDING);
                }
            }
            ctx.restore();
        });
        if (selected.canvasObjectWrapper !== null && !aabb(selected.canvasObjectWrapper.canvasObject, mouse, SELECT_PADDING)) {
            selected.canvasObjectWrapper = null;
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
                this.canvasInserts.push(canvasInsert);
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



canvas.addEventListener('mousemove', (event) => {
    mouse.x = event.offsetX;//todo left click drag only
    mouse.y = event.offsetY;
    mouse.dx += event.movementX;
    mouse.dy += event.movementY;
    if (Math.sqrt(Math.pow(mouse.dx, 2) + Math.pow(mouse.dy, 2)) > 30) {
        mouse.dx = 0;
        mouse.dy = 0;
        flushActive();
    }
    if (mouse.down) {
        mouse.drag = true;
    }
    if (mouse.drag) {
        if (selected.canvasObjectWrapper !== null) {
            selected.canvasObjectWrapper.canvasObject.x += event.movementX;
            selected.canvasObjectWrapper.canvasObject.y += event.movementY;
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
    if (selected.canvasObjectWrapper === null) {
        if ((event.buttons & 1) === 0) {
            let shape = Shape.create(event.offsetX, event.offsetY, 50, 50);
            insert(CanvasObjectType.SHAPE, shape);
        }
    }
});

canvas.addEventListener('contextmenu', (event) => {
    if (selected.canvasObjectWrapper !== null) {
        deletee(selected.id);
        event.preventDefault();
    }
});

function getDt() {
    let a = (window.performance.now() - lastUpdate) / MAX_TIME * 255;
    console.log('ENCODE' + (window.performance.now() - lastUpdate) + ', ' + a);
    return a;
}

export function insert(canvasObjectType, canvasObject) {
    let id = (Math.random() - 0.5) * 32000;
    activeDocument.canvas.canvasObjectWrappers.set(id, new CanvasObjectWrapper(canvasObjectType, canvasObject));
    updateCanvas.insert(canvasObjectType, id, canvasObject);
}

export function deletee(id) {
    activeDocument.canvas.canvasObjectWrappers.delete(id);
    updateCanvas.delete(id);
}

function flushActive() {
    if (activeDocument == null) {
        return;
    }
    if (selected.canvasObjectWrapper !== null) {
        if (selected.dirty) {
            selected.dirty = false;
            updateCanvas.move(selected.id, selected.canvasObjectWrapper.canvasObject);
        }
    }
}

const UPDATE_INTERVAL = 1000;
let lastUpdate = 0;

setInterval(localUpdate, UPDATE_INTERVAL);
export function localUpdate() {
    flushActive();
    lastUpdate = window.performance.now();
    if (updateCanvas.canvasInsertsMap.size > 0) {
        socket.queue(new ClientMessageCanvasInsert(updateCanvas.canvasInsertsMap));
    }
    if (updateCanvas.canvasMovesMap.size > 0) {
        socket.queue(new ClientMessageCanvasMove(updateCanvas.canvasMovesMap));
    }
    if (updateCanvas.canvasDeletes.length > 0) {
        socket.queue(new ClientMessageCanvasDelete(updateCanvas.canvasDeletes));
    }
    socket.flush();
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

