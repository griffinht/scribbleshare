import CanvasUpdate from "../CanvasUpdate.js";
import CanvasObject from "../../canvasObject/CanvasObject.js";
import ByteBuffer from "../../../protocol/ByteBuffer.js";
import {Canvas} from "../../Canvas.js";
import CanvasUpdateType from "../CanvasUpdateType.js";

export default class CanvasUpdateMove extends CanvasUpdate {
    canvasMoves: CanvasMove[];
    id: number;
    set: boolean;
    i: number;
    time: number;

    constructor(byteBuffer: ByteBuffer) {
        super(CanvasUpdateType.MOVE);
        this.canvasMoves = [];
        this.id = byteBuffer.readInt16();
        this.set = false;
        this.i = 0;
        this.time = 0;
        let length = byteBuffer.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasMoves.push(new CanvasMove(byteBuffer));
        }
    }

    move(time: number, canvasObject: CanvasObject) {
        this.canvasMoves.push(CanvasMove.create(time, canvasObject));
        this.time = time;
    }

    draw(canvas: Canvas, time: number) {
        let canvasObjectWrapper = canvas.canvasObjectWrappers.get(this.id);
        if (canvasObjectWrapper === undefined) {
            throw new Error('undefined for ' + this.id); //todo
        }
        if (!this.set) {
            this.set = true;
            canvasObjectWrapper.canvasObject.original = CanvasObject.clone(canvasObjectWrapper.canvasObject);
        }

        if (this.canvasMoves.length > 0) {
            if (this.canvasMoves[this.i].dt <= time) {
                canvasObjectWrapper.canvasObject.original = CanvasObject.clone(this.canvasMoves[this.i].canvasObject);
                this.i++;
            }
        }
        if (this.i > 0 && this.i < this.canvasMoves.length) {
            canvasObjectWrapper.canvasObject.lerp(this.canvasMoves[this.i].canvasObject, time / this.canvasMoves[this.i].dt);
        }
/*        if (this.first <= this.time) {

        }
        if (this.first === 0) {
            while (this.canvasMoves.length > 0) {
                //console.log(time, this.time, canvasMoves[0].dt, canvasMoves[0].canvasObject.x, canvasMoves[0].canvasObject.y);
                //console.log(canvasObjectWrapper.canvasObject.original, this.canvasMoves[0].canvasObject)
                if (this.canvasMoves.length > 0) {

                }
                if (this.canvasMoves[0].dt <= this.time) {
                    this.time -= this.canvasMoves[0].dt;
                    canvasObjectWrapper.canvasObject.original = this.canvasMoves[0].canvasObject;
                    this.canvasMoves.shift();
                } else {
                    break;
                }
            }

        }*/
        return this.canvasMoves[this.canvasMoves.length - 1].dt <= time;
    }

    serialize(byteBuffer: ByteBuffer) {
        super.serialize(byteBuffer);
        byteBuffer.writeInt16(this.id);
        byteBuffer.writeUint8(this.canvasMoves.length);
        this.canvasMoves.forEach((canvasMove) => {
            canvasMove.serialize(byteBuffer);
        });
    }

    static create(id: number, time: number) {
        let object = Object.create(this.prototype);
        object.canvasUpdateType = CanvasUpdateType.MOVE;
        object.time = 0;
        object.canvasMoves = [];
        object.first = time;
        object.time = time;
        object.id = id;
        return object;
    }
}

class CanvasMove {
    dt: number;
    canvasObject: CanvasObject;

    constructor(byteBuffer: ByteBuffer) {
        this.dt = byteBuffer.readUint8();
        this.canvasObject = new CanvasObject(byteBuffer);
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.dt);
        this.canvasObject.serialize(byteBuffer);
    }

    static create(dt: number, canvasObject: CanvasObject) {
        let canvasMove = Object.create(this.prototype);
        canvasMove.dt = dt;
        canvasMove.canvasObject = CanvasObject.clone(canvasObject);
        return canvasMove;
    }
}