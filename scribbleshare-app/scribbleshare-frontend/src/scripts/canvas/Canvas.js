import Shape, {ShapeType} from "./canvasObject/canvasObjects/Shape.js";
import {CanvasObjectType} from "./canvasObject/CanvasObjectType.js";
import {activeDocument, localClient, localClientId} from "../Document.js";
import socket from "../protocol/WebSocketHandler.js";
import CanvasObjectWrapper from "./canvasObject/CanvasObjectWrapper.js";
import ServerMessageType from "../protocol/server/ServerMessageType.js";
import ClientMessageCanvasUpdate from "../protocol/client/messages/ClientMessageCanvasUpdate.js";
import {getCanvasObject} from "./canvasObject/getCanvasObject.js";
import CanvasUpdateMove from "./canvasUpdate/canvasUpdates/CanvasUpdateMove.js";
import Mouse from "../Mouse.js";
import CanvasUpdateDelete from "./canvasUpdate/canvasUpdates/CanvasUpdateDelete.js";
import CanvasUpdateInsert from "./canvasUpdate/canvasUpdates/CanvasUpdateInsert.js";
import CanvasMouse from "./canvasObject/canvasObjects/CanvasMouse.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

const SELECT_PADDING = 10;

const mouse = new Mouse(canvas);

let canvasUpdates = [];//todo these could be instance variables but idk
let canvasUpdateMove = null;
let mouseUpdateMove = null;

export class Canvas {
    constructor(reader) {
        this.isOpen = false;
        this.last = 0;
        this.canvasObjectWrappers = new Map();
        this.canvasUpdates = [];
        this.selected = {
            id:0,
            canvasObjectWrapper:null,
            dirty:false,
        };
        this.localMouse = null;
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
        if (this.localMouse === null) {
            this.localMouse = CanvasMouse.create();
        }
        let canvasObjectWrapper = new CanvasObjectWrapper(CanvasObjectType.MOUSE, this.localMouse);
        activeDocument.canvas.canvasObjectWrappers.set(localClientId, canvasObjectWrapper);
        canvasUpdates.push(CanvasUpdateInsert.create(getNow(), localClientId, canvasObjectWrapper));
        window.requestAnimationFrame((now) => this.draw(now));
    }

    close() {
        this.isOpen = false;
        update();
    }

    draw(now) {
        if (!this.isOpen) {
            console.log(this.isOpen);
            return;
        }

        let dt = (now - this.last);
        let remoteTime = convertTime(now - lastRemoteUpdate);


        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.fillText('fps:' + (1000 / dt), 50, 150);
        ctx.fillText('' + remoteTime, 50, 100);
        ctx.fillText(mouse.dx + ', ' + mouse.dy, 50, 250);
        //console.log('draw1', this.canvasObjects);
        dt = convertTime(dt);
        for (let i = 0; i < this.canvasUpdates.length; i++) {
            if (this.canvasUpdates[i].draw(this, dt)) {
                this.canvasUpdates.splice(i--, 1);
            }
        }

        this.canvasObjectWrappers.forEach((canvasObjectWrapper, id) => {
            ctx.save();
            ctx.translate(canvasObjectWrapper.canvasObject.x, canvasObjectWrapper.canvasObject.y);
            ctx.rotate((canvasObjectWrapper.canvasObject.rotation / 255) * (2 * Math.PI));
            canvasObjectWrapper.canvasObject.draw();
            if (this.selected.canvasObjectWrapper === null) {
                if (!mouse.drag) {
                    if (aabb(canvasObjectWrapper.canvasObject, mouse, SELECT_PADDING)) {
                        this.selected.id = id;
                        this.selected.canvasObjectWrapper = canvasObjectWrapper;
                    }
                }
            } else {
                if (canvasObjectWrapper.canvasObject === this.selected.canvasObjectWrapper.canvasObject) {
                    ctx.strokeRect(0 - SELECT_PADDING / 2, 0 - SELECT_PADDING / 2, canvasObjectWrapper.canvasObject.width + SELECT_PADDING, canvasObjectWrapper.canvasObject.height + SELECT_PADDING);
                }
            }
            ctx.restore();
        });
        if (this.selected.canvasObjectWrapper !== null && !aabb(this.selected.canvasObjectWrapper.canvasObject, mouse, SELECT_PADDING)) {
            this.selected.canvasObjectWrapper = null;
        }

        this.last = now;
        window.requestAnimationFrame((now) => this.draw(now));
    }

    insert(canvasObjectType, canvasObject) {
        let id = (Math.random() - 0.5) * 32000;//todo i don't like this
        let canvasObjectWrapper = new CanvasObjectWrapper(canvasObjectType, canvasObject);
        activeDocument.canvas.canvasObjectWrappers.set(id, canvasObjectWrapper);
        canvasUpdates.push(CanvasUpdateInsert.create(getNow(), id, canvasObjectWrapper));
    }

    delete(id) {
        this.canvasObjectWrappers.delete(id);
        canvasUpdates.push(CanvasUpdateDelete.create(getNow(), id));
    }

    update(canvasUpdates) {
        canvasUpdates.forEach((canvasUpdate) => {
            this.canvasUpdates.push(canvasUpdate);
        })
    }

    flushActive() {
        if (this.selected.canvasObjectWrapper !== null) {
            if (this.selected.dirty) {
                this.selected.dirty = false;
                if (canvasUpdateMove !== null) {
                    canvasUpdateMove.move(getNow(), this.selected.canvasObjectWrapper.canvasObject);
                }
            }
        }
    }

    onEvent(event) {
        switch (event.type) {
            case 'mousemove': {
                this.localMouse.x = mouse.x;
                this.localMouse.y = mouse.y;
                if (Math.sqrt(Math.pow(mouse.dx, 2) + Math.pow(mouse.dy, 2)) > 30) {
                    this.flushActive();
                    if (mouse.dx > 0 || mouse.dy > 0) {
                        if (mouseUpdateMove === null) {
                            mouseUpdateMove = CanvasUpdateMove.create(localClientId, getNow());
                        }
                        mouseUpdateMove.move(getNow(), this.localMouse);
                    }
                    mouse.reset();
                }
                if (mouse.drag) {
                    if (this.selected.canvasObjectWrapper !== null) {
                        if (canvasUpdateMove === null) {
                            canvasUpdateMove = CanvasUpdateMove.create(this.selected.id, getNow());
                        }
                        this.selected.canvasObjectWrapper.canvasObject.x += event.movementX;
                        this.selected.canvasObjectWrapper.canvasObject.y += event.movementY;
                        this.selected.dirty = true;
                    } else {
                        ondrag(event);
                    }
                } else if (canvasUpdateMove !== null) {
                    canvasUpdates.push(canvasUpdateMove);
                    canvasUpdateMove = null;
                }
                break;
            }
            case 'click': {
                if (this.selected.canvasObjectWrapper === null) {
                    if ((event.buttons & 1) === 0) {
                        let shape = Shape.create(event.offsetX, event.offsetY, 50, 50, ShapeType.RECTANGLE);
                        this.insert(CanvasObjectType.SHAPE, shape);
                    }
                }
                break;
            }
            case 'contextmenu': {
                if (this.selected.canvasObjectWrapper !== null) {
                    this.delete(this.selected.id);
                    event.preventDefault();
                }
                break;
            }
        }
    }
}

function aabb(rect1, rect2, padding) {
    return rect1.x - padding < rect2.x + rect2.width &&
        rect1.x + rect1.width + padding > rect2.x &&
        rect1.y - padding < rect2.y + rect2.height &&
        rect1.y + rect1.height + padding > rect2.y;
}

window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
    let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    let rect = canvas.parentNode.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    ctx.putImageData(imageData, 0, 0);
}
resizeCanvas();





function onEvent(event) {
    if (activeDocument !== null) {
        activeDocument.canvas.onEvent(event);
    }
}

canvas.addEventListener('mousemove', onEvent);
canvas.addEventListener('click', onEvent);
canvas.addEventListener('contextmenu', onEvent);




export const UPDATE_INTERVAL = 1000;

let lastUpdate = 0;

function convertTime(time) {
    return time / UPDATE_INTERVAL * 255;
}

function getNow() {
    return convertTime(window.performance.now() - lastUpdate);
}

let lastRemoteUpdate = 0;
socket.addMessageListener(ServerMessageType.CANVAS_UPDATE, (serverMessageCanvasUpdate) => {
    //apply remote updates
    lastRemoteUpdate = window.performance.now();
    activeDocument.canvas.update(serverMessageCanvasUpdate.canvasUpdates);
});
socket.addMessageListener(ServerMessageType.MOUSE_MOVE, (a) => {
    lastRemoteUpdate = window.performance.now();
})

function update() {
    if (activeDocument !== null) {
        activeDocument.canvas.flushActive();

        //assemble local updates
        if (canvasUpdateMove !== null) {
            canvasUpdates.push(canvasUpdateMove);
            canvasUpdateMove = null;
        }
        if (mouseUpdateMove !== null) {
            canvasUpdates.push(mouseUpdateMove);
            mouseUpdateMove = null;
        }

        //send local updates
        if (canvasUpdates.length > 0) {
            socket.queue(new ClientMessageCanvasUpdate(canvasUpdates));
        }
        socket.flush();
        lastUpdate = window.performance.now();

        //clean up
        canvasUpdates.length = 0;
    } else {
        //happens if there is no document open
    }
}
setInterval(update, UPDATE_INTERVAL);


function ondrag(event) {
    //todo draw line
}


export function lerp(v0, v1, t) {
    return v0 * (1 - t) + v1 * t;
}