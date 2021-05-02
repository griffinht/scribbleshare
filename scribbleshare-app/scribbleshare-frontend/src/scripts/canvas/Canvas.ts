import Shape, {ShapeType} from "./canvasObject/canvasObjects/Shape.js";
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
import Line from "./canvasObject/canvasObjects/Line.js";
import EntityCanvasObject from "./canvasObject/EntityCanvasObject.js";
import color from "../ColorSelector.js";
import shapee from "../ShapeSelector.js";
import ByteBuffer from "../protocol/ByteBuffer";
import CanvasUpdate from "./canvasUpdate/CanvasUpdate";
import CanvasObject from "./canvasObject/CanvasObject";
import ServerMessageCanvasUpdate from "../protocol/server/messages/ServerMessageCanvasUpdate";
import ServerMessage from "../protocol/server/ServerMessage";
import CanvasObjectType from "./canvasObject/CanvasObjectType";

export const canvas = document.getElementById('canvas')! as HTMLCanvasElement;
export const ctx = canvas.getContext('2d')!;

const SELECT_PADDING = 10;

const mouse = new Mouse(canvas);

let canvasUpdatesArray: CanvasUpdate[] = [];//todo these could be instance variables but idk
let canvasUpdateMove: CanvasUpdateMove | null = null;
let mouseUpdateMove: CanvasUpdateMove | null = null;
let line: Line | null = null;
let leftLock = false;
let rightLock = false;

export class Canvas {
    isOpen: boolean;
    last: number;
    lastFlushSelected: number;
    lastFlushMouse: number;
    lastFlushLine: number;
    canvasObjectWrappers: Map<number, CanvasObjectWrapper>;
    canvasUpdates: Array<CanvasUpdate>;
    selected: {
        id: number;
        canvasObjectWrapper: CanvasObjectWrapper | null;
        dirty: boolean;
    }
    localMouse: Mouse | null;

    constructor(byteBuffer?: ByteBuffer) {
        this.isOpen = false;
        this.last = 0;
        this.lastFlushSelected = 0;
        this.lastFlushMouse = 0;
        this.lastFlushLine = 0;
        this.canvasObjectWrappers = new Map();
        this.canvasUpdates = [];
        this.selected = {
            id:0,
            canvasObjectWrapper:null,
            dirty:false,
        };
        this.localMouse = null;
        if (byteBuffer != null) {
            let length = byteBuffer.readUint8();
            for (let i = 0; i < length; i++) {
                let type = byteBuffer.readUint8();
                let lengthJ = byteBuffer.readUint16();
                for (let j = 0; j < lengthJ; j++) {
                    this.canvasObjectWrappers.set(byteBuffer.readInt16(), new CanvasObjectWrapper(type, getCanvasObject(type, byteBuffer)));
                }
            }
        }
    }

    open() {
        this.isOpen = true;
/*        if (this.localMouse === null) {
            this.localMouse = CanvasMouse.create();
        }*/
/*        let canvasObjectWrapper = new CanvasObjectWrapper(CanvasObjectType.MOUSE, this.localMouse);
        activeDocument.canvas.canvasObjectWrappers.set(localClientId, canvasObjectWrapper);*/
/*        canvasUpdates.push(CanvasUpdateInsert.create(getNow(), localClientId, canvasObjectWrapper));*/
        window.requestAnimationFrame((now) => this.draw(now));
    }

    close() {
        this.isOpen = false;
        update();
    }

    draw(now: number) {
        if (!this.isOpen) {
            console.log(this.isOpen);
            return;
        }

        this.flush();

        let dt = (now - this.last);
        let remoteTime = convertTime(now - lastRemoteUpdate);


        ctx.clearRect(0, 0, canvas.width, canvas.height);
/*        ctx.fillText('fps:' + (1000 / dt), 50, 150);
        ctx.fillText('' + remoteTime, 50, 100);
        ctx.fillText(mouse.dx + ', ' + mouse.dy, 50, 250);*/
        //console.log('draw1', this.canvasObjects);
        dt = convertTime(dt);
        for (let i = 0; i < this.canvasUpdates.length; i++) {
            if (this.canvasUpdates[i].draw(this, remoteTime)) {
                this.canvasUpdates.splice(i--, 1);
            }
        }

        this.canvasObjectWrappers.forEach((canvasObjectWrapper, id) => {
            if (id !== localClientId) {
                if (canvasObjectWrapper.canvasObject instanceof EntityCanvasObject) {
                    ctx.save();
                    ctx.translate(canvasObjectWrapper.canvasObject.x, canvasObjectWrapper.canvasObject.y);
                    //ctx.rotate((canvasObjectWrapper.canvasObject.rotation / 255) * (2 * Math.PI));
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
                } else {
                    canvasObjectWrapper.canvasObject.draw();
                }
            }
        });
        if (this.selected.canvasObjectWrapper !== null
            && this.selected.canvasObjectWrapper.canvasObject instanceof EntityCanvasObject
            && !aabb(this.selected.canvasObjectWrapper.canvasObject, mouse, SELECT_PADDING)) {
            this.selected.canvasObjectWrapper = null;
        }

        this.last = now;
        window.requestAnimationFrame((now) => this.draw(now));
    }

    insert(type: CanvasObjectType, canvasObject: CanvasObject) {
        let id = (Math.random() - 0.5) * 32000;//todo i don't like this
        let canvasObjectWrapper = new CanvasObjectWrapper(type, canvasObject);
        // @ts-ignore todo
        activeDocument.canvas.canvasObjectWrappers.set(id, canvasObjectWrapper);
        //canvasUpdates.push(CanvasUpdateInsert.create(getNow(), id, canvasObjectWrapper));
    }

    delete(id: number) {
        this.canvasObjectWrappers.delete(id);
        //canvasUpdates.push(CanvasUpdateDelete.create(getNow(), id));
    }

    update(canvasUpdates: Array<CanvasUpdate>) {
        canvasUpdates.forEach((canvasUpdate) => {
            this.canvasUpdates.push(canvasUpdate);
        })
    }

    flush() {
        this.flushSelected();
        this.flushMouse();
        this.flushLine();
    }

    flushSelected() {
        if (this.selected.canvasObjectWrapper !== null) {
            if (this.selected.dirty) {
                if (canvasUpdateMove !== null) {
                    this.selected.dirty = false;
                    canvasUpdateMove.move(getNow(), this.selected.canvasObjectWrapper.canvasObject);
                    this.lastFlushSelected = window.performance.now();
                }
            }
        }

    }

    flushMouse() {
/*        if (false && mouseUpdateMove !== null) {
            if (((Math.abs(mouse.dx) > 0 || Math.abs(mouse.dy) > 0) && this.last - this.lastFlushMouse > 100)
                || (Math.sqrt(Math.pow(mouse.dx, 2) + Math.pow(mouse.dy, 2)) > 30)) {
                mouseUpdateMove.move(getNow(), this.localMouse);
                //console.log(this.last - this.lastFlushMouse)
                this.lastFlushMouse = window.performance.now();
                mouse.reset();
            }
        }*/
    }

    flushLine() {

        this.lastFlushLine = window.performance.now();
    }

    onEvent(event: MouseEvent) {
        switch (event.type) {
            case 'mousemove': {
/*                this.localMouse.x = mouse.x;
                this.localMouse.y = mouse.y;*/
                //mouseUpdateMove = CanvasUpdateMove.create(localClientId, getNow());

                //console.log(mouse.drag, (event.buttons & 1))
                if (mouse.drag) {


                    if ((event.buttons & 1) !== 1) {
                        rightLock = true;
                        if (this.selected.canvasObjectWrapper !== null && this.selected.canvasObjectWrapper.canvasObject instanceof EntityCanvasObject) {
                            this.selected.canvasObjectWrapper.canvasObject.width += event.movementX;
                            this.selected.canvasObjectWrapper.canvasObject.height += event.movementY;
                            //canvasUpdates.push(CanvasUpdateInsert.create(getNow(), this.selected.id, this.selected.canvasObjectWrapper));
                        }
                    } else {
                        //leftLock = true;
                        if (this.selected.canvasObjectWrapper !== null) {
                            if (canvasUpdateMove === null) {
                                canvasUpdateMove = CanvasUpdateMove.create(this.selected.id, getNow());
                            }
                            this.selected.canvasObjectWrapper.canvasObject.x += event.movementX;
                            this.selected.canvasObjectWrapper.canvasObject.y += event.movementY;
                            this.selected.dirty = true;
                        } else {
                            if (line === null) {
                                line = Line.create(event.offsetX, event.offsetY, color);
                                this.insert(CanvasObjectType.LINE, line);
                            } else {
                                line.pushPoint(event.offsetX, event.offsetY);
                                this.flushLine();
                            }
                        }
                    }
                } else if (canvasUpdateMove !== null && ((event.buttons & 1) !== 1)) {
                    //canvasUpdates.push(canvasUpdateMove);
                    canvasUpdateMove = null;
                }
                break;
            }
            case 'mouseup': {
                if (line !== null) {
                    line = null;
                    leftLock = true;
                } else {
                    leftLock = false;
                }
                break;
            }
            case 'click': {
                if (leftLock) {
                    leftLock = true;
                    return;
                } else {
                    console.log('click');
                }
                if (this.selected.canvasObjectWrapper === null) {
                    if ((event.buttons & 1) === 0) {
                        let s = Shape.create(event.offsetX, event.offsetY, 50, 50, shapee.a, color);
                        this.insert(CanvasObjectType.SHAPE, s);
                    }
                }
                break;
            }
            case 'contextmenu': {
                console.log(rightLock);
                if (rightLock) {
                    event.preventDefault();
                    rightLock = false;
                    return;
                }
                if (this.selected.canvasObjectWrapper !== null) {
                    this.delete(this.selected.id);
                    event.preventDefault();
                }
                break;
            }
        }
    }
}

interface Rectangle {
    x: number;
    y: number;
    width: number;
    height: number;
}

function aabb(rect1: Rectangle, rect2: Rectangle, padding: number) {
    return rect1.x - padding < rect2.x + rect2.width &&
        rect1.x + rect1.width + padding > rect2.x &&
        rect1.y - padding < rect2.y + rect2.height &&
        rect1.y + rect1.height + padding > rect2.y;
}

window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
    let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    // @ts-ignore
    let rect = canvas.parentNode.getBoundingClientRect();//todo
    canvas.width = rect.width;
    canvas.height = rect.height;
    ctx.putImageData(imageData, 0, 0);
}
resizeCanvas();





function onEvent(event: MouseEvent) {
    if (activeDocument !== null) {
        activeDocument.canvas.onEvent(event);
    }
}

canvas.addEventListener('mousemove', onEvent);
canvas.addEventListener('mouseup', onEvent);
canvas.addEventListener('click', onEvent);
canvas.addEventListener('contextmenu', onEvent);




export const UPDATE_INTERVAL = 1000;

let lastUpdate = 0;

function convertTime(time: number) {
    return time / UPDATE_INTERVAL * 255;
}

function getNow() {
    return convertTime(window.performance.now() - lastUpdate);
}

let lastRemoteUpdate = 0;
socket.addMessageListener(ServerMessageType.CANVAS_UPDATE, (serverMessage: ServerMessage) => {
    let serverMessageCanvasUpdate = serverMessage as ServerMessageCanvasUpdate;
    //apply remote updates
    lastRemoteUpdate = window.performance.now();
    // @ts-ignore todo
    activeDocument.canvas.update(serverMessageCanvasUpdate.canvasUpdates);
});
socket.addMessageListener(ServerMessageType.MOUSE_MOVE, () => {
    lastRemoteUpdate = window.performance.now();
})

function update() {
    if (activeDocument !== null) {
        activeDocument.canvas.flush();

        //assemble local updates
        if (canvasUpdateMove !== null) {
            //canvasUpdates.push(canvasUpdateMove);
            canvasUpdateMove = null;
        }
/*        if (mouseUpdateMove !== null) {
            canvasUpdates.push(mouseUpdateMove);
            mouseUpdateMove = null;
        }*/

        //send local updates
        /*if (canvasUpdates.length > 0) {
            socket.queue(new ClientMessageCanvasUpdate(canvasUpdates));
        }*/
        socket.flush();
        lastUpdate = window.performance.now();

        //clean up
        //canvasUpdates.length = 0;
    } else {
        //happens if there is no document open
    }
}
setInterval(update, UPDATE_INTERVAL);



export function lerp(v0: number, v1: number, t: number) {
    return v0 * (1 - t) + v1 * t;
}