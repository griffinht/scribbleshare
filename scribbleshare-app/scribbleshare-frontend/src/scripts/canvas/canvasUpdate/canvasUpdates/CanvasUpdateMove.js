import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import CanvasObject from "../../canvasObject/CanvasObject.js";

export default class CanvasUpdateMove extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.MOVE);
        this.canvasMoves = [];
        this.time = 0;
        this.id = reader.readInt16();
        this.first = reader.readUint8();
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            this.canvasMoves.push(new CanvasMove(reader));
        }
    }

    move(time, canvasObject) {
        this.canvasMoves.push(CanvasMove.create(time - this.time, canvasObject));
        this.time = time;
    }

    draw(canvas, time) {
        this.time += time;
        let canvasObjectWrapper = canvas.canvasObjectWrappers.get(this.id);

        if (this.first !== 0 && this.first <= this.time) {
            this.time -= this.first;
            this.first = 0;
            canvasObjectWrapper.canvasObject.original = CanvasObject.clone(canvasObjectWrapper.canvasObject);
        }
        if (this.first === 0) {
            while (this.canvasMoves.length > 0) {
                //console.log(time, this.time, canvasMoves[0].dt, canvasMoves[0].canvasObject.x, canvasMoves[0].canvasObject.y);
                //console.log(canvasObjectWrapper.canvasObject.original, this.canvasMoves[0].canvasObject)
                if (this.canvasMoves.length > 0) {
                    canvasObjectWrapper.canvasObject.lerp(this.canvasMoves[0].canvasObject, this.time / this.canvasMoves[0].dt);
                }
                if (this.canvasMoves[0].dt <= this.time) {
                    this.time -= this.canvasMoves[0].dt;
                    canvasObjectWrapper.canvasObject.original = this.canvasMoves[0].canvasObject;
                    this.canvasMoves.shift();
                } else {
                    break;
                }
            }

        }
        return this.canvasMoves.length === 0;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeInt16(this.id);
        writer.writeUint8(this.first);
        writer.writeUint8(this.canvasMoves.length);
        this.canvasMoves.forEach((canvasMove) => {
            canvasMove.serialize(writer);
        });
    }

    static create(id, time) {
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
    constructor(reader) {
        this.dt = reader.readUint8();
        this.canvasObject = new CanvasObject(reader);
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        this.canvasObject.serialize(writer);
    }

    static create(dt, canvasObject) {
        let canvasMove = Object.create(this.prototype);
        canvasMove.dt = dt;
        canvasMove.canvasObject = CanvasObject.clone(canvasObject);
        return canvasMove;
    }
}