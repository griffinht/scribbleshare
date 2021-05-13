import {activeDocument, localClientId} from "../Document.js";
import socket from "../protocol/WebSocketHandler.js";
import CanvasObjectWrapper from "./canvasObject/CanvasObjectWrapper.js";
import ServerMessageType from "../protocol/server/ServerMessageType.js";
import {getCanvasObject} from "./canvasObject/getCanvasObject.js";
import EntityCanvasObject from "./canvasObject/EntityCanvasObject.js";
import ByteBuffer from "../protocol/ByteBuffer.js";
import CanvasUpdate from "./canvasUpdate/CanvasUpdate.js";
import CanvasObject from "./canvasObject/CanvasObject.js";
import ServerMessageCanvasUpdate from "../protocol/server/messages/ServerMessageCanvasUpdate.js";
import ServerMessage from "../protocol/server/ServerMessage.js";
import CanvasObjectType from "./canvasObject/CanvasObjectType.js";
import Mouse from "../Mouse.js";
import Shape from "./canvasObject/canvasObjects/Shape.js";
import color from "../ColorSelector.js";
import shape from "../ShapeSelector.js";

export const canvas = document.getElementById('canvas')! as HTMLCanvasElement;
export const ctx = canvas.getContext('2d')!;

const mouse = new Mouse(canvas);

const SELECT_PADDING = 10;

class Selected {
    id: number = 0;
    canvasObjectWrapper: CanvasObjectWrapper | null = null;

    update(id: number, canvasObjectWrapper: CanvasObjectWrapper) {
        this.id = id;
        this.canvasObjectWrapper = canvasObjectWrapper;
    }

    get(): CanvasObjectWrapper {
        if (this.canvasObjectWrapper === null) throw new Error('tried to get when there is nothing to get');
        return this.canvasObjectWrapper;
    }

    has(): boolean {
        return this.canvasObjectWrapper !== null;
    }

    clear() {
        this.id = 0;
        this.canvasObjectWrapper = null;
    }
}

let selected = new Selected();

export class Canvas {
    isOpen: boolean;
    last: number;
    canvasObjectWrappers: Map<number, CanvasObjectWrapper>;
    canvasUpdates: Array<CanvasUpdate>;

    constructor(byteBuffer?: ByteBuffer) {
        this.isOpen = false;
        this.last = 0;
        this.canvasObjectWrappers = new Map();
        this.canvasUpdates = [];
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
                    if (!selected.has()) {
                        if (aabb(canvasObjectWrapper.canvasObject, mouse, SELECT_PADDING)) {
                            selected.update(id, canvasObjectWrapper);
                        }
                    }
                    /*if (this.selected.g.canvasObjectWrapper === null) {
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
                    }*/
                    ctx.restore();
                } else {
                    canvasObjectWrapper.canvasObject.draw();
                }
            }
        });
        //clear selection if the mouse is gone
        if (selected.has()
            && selected.get().canvasObject instanceof EntityCanvasObject
            && !aabb(selected.get().canvasObject as EntityCanvasObject, mouse, SELECT_PADDING)) {
            selected.clear();
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
        if (selected.has() && selected.id === id) {
            selected.clear();
        }
        this.canvasObjectWrappers.delete(id);
        //canvasUpdates.push(CanvasUpdateDelete.create(getNow(), id));
    }

    update(canvasUpdates: Array<CanvasUpdate>) {
        canvasUpdates.forEach((canvasUpdate) => {
            this.canvasUpdates.push(canvasUpdate);
        })
    }

    flush() {

    }
}

interface Rectangle {
    x: number;
    y: number;
    width: number;
    height: number;
}

class Point implements Rectangle {
    height: number = 0;
    width: number = 0;
    x: number;
    y: number;

    constructor(x: number, y: number) {
        this.x = x;
        this.y = y;
    }
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

mouse.addEventListener('click', (event) => {
    if (activeDocument !== null) {
        let object = Shape.create(event.offsetX, event.offsetY, 50, 50, shape, color)
        activeDocument.canvas.insert(CanvasObjectType.SHAPE, object);
    }
});

mouse.addEventListener('contextmenu', (event) => {
    if (activeDocument !== null && selected.has()) {
        activeDocument.canvas.delete(selected.id);
    }
});