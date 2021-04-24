import Shape from "./canvasObject/canvasObjects/Shape.js";
import {CanvasObjectType} from "./canvasObject/CanvasObjectType.js";
import {activeDocument, localClient} from "../Document.js";
import socket from "../protocol/WebSocketHandler.js";
import CanvasObjectWrapper from "./canvasObject/CanvasObjectWrapper.js";
import ServerMessageType from "../protocol/server/ServerMessageType.js";
import ClientMessageCanvasUpdate from "../protocol/client/messages/ClientMessageCanvasUpdate.js";
import CanvasUpdateDelete from "./canvasUpdate/canvasUpdates/CanvasUpdateDelete.js";
import {getCanvasObject} from "./canvasObject/getCanvasObject.js";
import CanvasUpdateMove from "./canvasUpdate/canvasUpdates/CanvasUpdateMove.js";
import CanvasUpdateInsert from "./canvasUpdate/canvasUpdates/CanvasUpdateInsert.js";
import Mouse from "../Mouse.js";
import MouseMove from "../MouseMove.js";
import ClientMessageMouseMove from "../protocol/client/messages/ClientMessageMouseMove.js";

export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

const SELECT_PADDING = 10;

const mouse = new Mouse(canvas);

let canvasUpdateInsert = CanvasUpdateInsert.create();//todo these could be instance variables but idk
let canvasUpdateMove = CanvasUpdateMove.create();
let canvasUpdateDelete = CanvasUpdateDelete.create();

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
            this.canvasUpdates[i].draw(this, dt)
            if (!this.canvasUpdates[i].isDirty()) {
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
        activeDocument.clients.forEach((client) => {
            if (client !== localClient) {
                ctx.fillRect(client.mouseX, client.mouseY, 5, 5);
                while (client.mouseMoves.length > 0) {
                    //todo lerp
                    client.time += dt;
                    if (client.mouseMoves[0].dt <= client.time) {
                        client.time -= client.mouseMoves[0].dt;
                        client.mouseX = client.mouseMoves[0].x;
                        client.mouseY = client.mouseMoves[0].y;
                        console.log(client.time, getNow(), dt, client.mouseMoves[0]);
                        client.mouseMoves.shift();
                    } else {
                        break;
                    }
                }
            }
        })

        this.last = now;
        window.requestAnimationFrame((now) => this.draw(now));
    }

    insert(canvasObjectType, canvasObject) {
        let id = (Math.random() - 0.5) * 32000;//todo i don't like this
        activeDocument.canvas.canvasObjectWrappers.set(id, new CanvasObjectWrapper(canvasObjectType, canvasObject));
        canvasUpdateInsert.insert(canvasObjectType, getNow(), id, canvasObject);
    }

    move(id, canvasObject) {
        canvasUpdateMove.move(id, getNow(), canvasObject);
    }

    delete(id) {
        this.canvasObjectWrappers.delete(id);
        canvasUpdateDelete.delete(getNow(), id);
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
                this.move(this.selected.id, this.selected.canvasObjectWrapper.canvasObject);
            }
        }
    }

    onEvent(event) {
        switch (event.type) {
            case 'mousemove': {
                if (Math.sqrt(Math.pow(mouse.dx, 2) + Math.pow(mouse.dy, 2)) > 30) {
                    this.flushActive();
                    if (mouse.dx > 0 || mouse.dy > 0) {
                        let mouseMove = MouseMove.create(getNow() - localClient.time, mouse.x, mouse.y);
                        localClient.time = getNow();

                        localClient.mouseMoves.push(mouseMove);
                    }
                    mouse.reset();
                }
                if (mouse.drag) {
                    if (this.selected.canvasObjectWrapper !== null) {
                        this.selected.canvasObjectWrapper.canvasObject.x += event.movementX;
                        this.selected.canvasObjectWrapper.canvasObject.y += event.movementY;
                        this.selected.dirty = true;
                    } else {
                        ondrag(event);
                    }
                }
                break;
            }
            case 'click': {
                if (this.selected.canvasObjectWrapper === null) {
                    if ((event.buttons & 1) === 0) {
                        let shape = Shape.create(event.offsetX, event.offsetY, 50, 50);
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

function update() {
    if (activeDocument !== null) {
        activeDocument.canvas.flushActive();

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

        if (localClient.mouseMoves.length > 0 && activeDocument.clients.size > 1) {
            socket.send(new ClientMessageMouseMove(localClient.mouseMoves));
        }

        //send local updates
        if (canvasUpdates.length > 0) {
            socket.queue(new ClientMessageCanvasUpdate(canvasUpdates));
        }
        socket.flush();
        lastUpdate = window.performance.now();

        //clean up
        localClient.mouseMoves.length = 0;
        localClient.time = 0;
        canvasUpdates.forEach((canvasUpdate) => {
            canvasUpdate.clear();
        })
        canvasUpdates.length = 0;
    } else {
        //shouldn't happen
        console.warn('update with no open document');
    }
}
setInterval(update, UPDATE_INTERVAL);


function ondrag(event) {
    //todo draw line
}
