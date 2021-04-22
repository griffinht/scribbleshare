import Shape from "./canvasObject/canvasObjects/Shape.js";
import {CanvasObjectType} from "./canvasObject/CanvasObjectType.js";
import {activeDocument} from "../Document.js";
import socket from "../protocol/WebSocketHandler.js";
import CanvasObjectWrapper from "./canvasObject/CanvasObjectWrapper.js";
import ServerMessageType from "../protocol/server/ServerMessageType.js";
import ClientMessageCanvasUpdate from "../protocol/client/messages/ClientMessageCanvasUpdate.js";
import CanvasUpdateDelete from "./canvasUpdate/canvasUpdates/CanvasUpdateDelete.js";
import {getCanvasObject} from "./canvasObject/getCanvasObject.js";
import CanvasUpdateMove from "./canvasUpdate/canvasUpdates/CanvasUpdateMove.js";
import CanvasUpdateInsert from "./canvasUpdate/canvasUpdates/CanvasUpdateInsert.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

let selected = {
    id:0,
    canvasObjectWrapper:null,
    dirty:false,
};

export class Canvas {
    constructor(reader) {
        this.isOpen = false;
        this.last = 0;
        this.canvasObjectWrappers = new Map();
        this.canvasUpdates = [];
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

    open() {
        this.isOpen = true;
        window.requestAnimationFrame(this.draw);
    }

    close() {
        this.isOpen = false;
    }

    draw(now) {
        if (!this.isOpen) {
            return;
        }

        let dt = (now - this.last);
        this.last = now;

        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.fillText('fps:' + (1000 / dt), 50, 150);
        dt = convertTime(dt);
        ctx.fillText('' + dt, 50, 100);
        //console.log('draw1', this.canvasObjects);

        for (let i = 0; i < this.canvasUpdates.length; i++) {
            this.canvasUpdates[i].draw(this, dt)
            if (!this.canvasUpdates[i].isDirty()) {
                this.canvasUpdates.slice(i--, 1);
            }
        }

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

        window.requestAnimationFrame(this.draw);
    }

    update(canvasUpdates) {
        canvasUpdates.forEach((canvasUpdate) => {
            this.canvasUpdates.push(canvasUpdate);
        })
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







let canvasUpdateInsert = CanvasUpdateInsert.create();
export function insert(canvasObjectType, canvasObject) {
    let id = (Math.random() - 0.5) * 32000;//todo i don't like this
    activeDocument.canvas.canvasObjectWrappers.set(id, new CanvasObjectWrapper(canvasObjectType, canvasObject));
    canvasUpdateInsert.insert(canvasObjectType,getNow(), id, canvasObject);
}

let canvasUpdateMove = CanvasUpdateMove.create();
function move(id, canvasObject) {
    canvasUpdateMove.move(id, getNow(), canvasObject);
}

let canvasUpdateDelete = CanvasUpdateDelete.create();
function deletee(id) {
    activeDocument.canvas.canvasObjectWrappers.delete(id);
    canvasUpdateDelete.delete(getNow(), id);
}



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

function flushActive() {
    if (activeDocument == null) {
        return;
    }
    if (selected.canvasObjectWrapper !== null) {
        if (selected.dirty) {
            selected.dirty = false;
            move(selected.id, selected.canvasObjectWrapper.canvasObject);
        }
    }
}

export const UPDATE_INTERVAL = 1000;

let lastUpdate = 0;

function convertTime(time) {
    return time / UPDATE_INTERVAL * 255;
}

function getNow() {
    return convertTime(window.performance.now()) - lastUpdate;
}

socket.addMessageListener(ServerMessageType.CANVAS_UPDATE, (serverMessageCanvasUpdate) => {
    if (activeDocument !== null) {
        flushActive();

        //assemble local updates
        let canvasUpdates = [];
        if (canvasUpdateInsert.isDirty()) {
            canvasUpdates.push(canvasUpdateInsert);
        }
        if (canvasUpdateMove.isDirty()) {
            canvasUpdates.push(canvasUpdateMove);
        }
        if (canvasUpdateDelete.isDirty()) {
            canvasUpdates.push(canvasUpdateDelete);
        }

        //send local updates
        socket.send(new ClientMessageCanvasUpdate(canvasUpdates));
        lastUpdate = convertTime(window.performance.now());

        //clean up
        canvasUpdates.forEach((canvasUpdate) => {
            canvasUpdate.clear();
        })
        canvasUpdates.length = 0;

        //apply remote updates
        activeDocument.canvas.update(serverMessageCanvasUpdate.canvasUpdates);
    } else {
        //shouldn't happen
        console.warn('update with no open document');
    }
});



function ondrag(event) {
    //todo draw line
}

