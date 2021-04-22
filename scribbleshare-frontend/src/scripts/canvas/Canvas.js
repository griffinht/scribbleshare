import Shape from "./canvasObject/canvasObjects/Shape.js";
import {CanvasObjectType} from "./canvasObject/CanvasObjectType.js";
import {activeDocument} from "../Document.js";
import socket from "../protocol/WebSocketHandler.js";
import CanvasObjectWrapper from "./canvasObject/CanvasObjectWrapper.js";
import ServerMessageType from "../protocol/server/ServerMessageType.js";
import ClientMessageCanvasUpdate from "../protocol/client/messages/ClientMessageCanvasUpdate.js";

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
        ctx.fillText('fps:' + (1000 / dt), 50, 150);
        dt = dt / MAX_TIME * 255;
        ctx.fillText('' + dt, 50, 100);
        a += dt;
        if (a > 255) {
            a = 0;
        }
        ctx.fillText('' + a, 50 , 200);
        for (let i = 0; i < this.canvasInserts.length; i++) {
            //console.log('inserts', this.canvasInserts[i].dt);
            this.canvasInserts[i].dt -= dt;
            if (this.canvasInserts[i].dt <= 0) {
                this.canvasObjectWrappers.set(this.canvasInserts[i].id, this.canvasInserts[i].canvasObjectWrapper);
                this.canvasInserts.splice(i--, 1);
            }
        }
        this.canvasMovesMap.forEach((canvasMoves, id) => {
            let canvasObjectWrapper = this.canvasObjectWrappers.get(id);
            if (canvasObjectWrapper === undefined) {
                console.warn('oopsie');
                return;
            }

            if (canvasMoves.length === 0) {
                canvasObjectWrapper.canvasObject.dt = 0;
                this.canvasMovesMap.delete(id);
            } else {
                ctx.fillText('' + canvasObjectWrapper.canvasObject.dt + ', ' + canvasMoves[0].dt, 300, 50)
                canvasObjectWrapper.canvasObject.dt += dt;
                if (canvasObjectWrapper.canvasObject.dt > canvasMoves[0].dt) {
                    console.log(canvasObjectWrapper.canvasObject.dt, canvasMoves[0].dt);
                    canvasObjectWrapper.canvasObject.dt = 0;
                    canvasObjectWrapper.canvasObject.original = canvasMoves[0].canvasObject;
                    canvasMoves.shift();
                }
                if (canvasMoves.length > 0) {
                    ctx.fillText('' + canvasObjectWrapper.canvasObject.dt / canvasMoves[0].dt, 50, 50)
                    canvasObjectWrapper.canvasObject.lerp(canvasMoves[0].canvasObject, canvasObjectWrapper.canvasObject.dt / canvasMoves[0].dt);
                }//will be removed on the next go around
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
    }

    update(canvasUpdates) {
        canvasUpdates.forEach((canvasUpdate) => {
            canvasUpdate.update(this);
        });
    }

    resize() {

        //todo redraw?
    }
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

        let dt = getNow();
        if (canvasMoves.length > 0) {
            dt -= canvasMoves[0].dt;
        }
        console.log(dt);
        canvasMoves.push(CanvasMove.create(dt, canvasObject));
    }

    delete(id) {
        this.canvasDeletes.push(CanvasDelete.create(getNow(), id))
    }

    clear() {
        this.canvasInsertsMap.clear();
        this.canvasMovesMap.clear();
        this.canvasDeletes.length = 0;
    }
}
//todo move to canvas class and turn on/off
let lastDraw = performance.now();
function draw(now) {
    let dt = (now - lastDraw);
    lastDraw = now;

    if (activeDocument != null) {
        activeDocument.canvas.draw(dt);
    }

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);

let updateCanvas = new UpdateCanvas();

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

export const UPDATE_INTERVAL = 1000;

function getNow() {
    return convertTime(window.performance.now());
}

function convertTime(time) {
    return time / UPDATE_INTERVAL * 255;
}

let canvasUpdates = [];
let lastUpdate = 0;

socket.addMessageListener(ServerMessageType.CANVAS_UPDATE, (serverMessageCanvasUpdate) => {
    if (activeDocument !== null) {
        activeDocument.canvas.update(serverMessageCanvasUpdate.canvasUpdates);
        socket.send(new ClientMessageCanvasUpdate(canvasUpdates));
        canvasUpdates.length = 0;
        lastUpdate = convertTime(window.performance.now());
    }
});

window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
    let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    let rect = canvas.parentNode.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    ctx.putImageData(imageData, 0, 0);
    if (activeDocument != null) {
        activeDocument.canvas.resize();
    }
}
resizeCanvas();

function ondrag(event) {
    //todo draw line
}

