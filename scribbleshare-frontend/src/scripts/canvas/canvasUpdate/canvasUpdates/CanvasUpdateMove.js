import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import CanvasObject from "../../canvasObject/CanvasObject.js";
import {ctx} from "../../Canvas.js";

export default class CanvasUpdateMove extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.MOVE);
        this.canvasMovesMap = new Map();
        this.time = 0;
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            let id = reader.readInt16();
            let canvasMoves = [];
            let lengthJ = reader.readUint8();
            for (let j = 0; j < lengthJ; j++) {
                canvasMoves.push(new CanvasMove(reader));
            }
            this.canvasMovesMap.set(id, canvasMoves);
        }
    }

    isDirty() {
        return this.canvasMovesMap.size > 0;
    }

    clear() {
        this.canvasMovesMap.clear();
        this.time = 0;
    }

    move(id, time, canvasObject) {
        let canvasMoves = this.canvasMovesMap.get(id);
        if (canvasMoves === undefined) {
            canvasMoves = [];
            this.canvasMovesMap.set(id, canvasMoves);
            return;
        }

        canvasMoves.push(CanvasMove.create(time - this.time, canvasObject));
        this.time = time;
    }

    draw(canvas, time) {
        this.time += time;
        this.canvasMovesMap.forEach((canvasMoves, id) => {
            let canvasObjectWrapper = canvas.canvasObjectWrappers.get(id);
            if (canvasObjectWrapper === undefined) {
                this.clear();
            }

            while (canvasMoves.length > 0) {
                console.log(time, this.time, canvasMoves[0].dt, canvasMoves[0].canvasObject.x, canvasMoves[0].canvasObject.y);
                canvasObjectWrapper.canvasObject.lerp(canvasMoves[0].canvasObject, this.time / canvasMoves[0].dt);
                if (canvasMoves[0].dt <= this.time) {
                    this.time -= canvasMoves[0].dt;
                    canvasObjectWrapper.canvasObject.original = canvasMoves[0].canvasObject;
                    canvasMoves.shift();
                } else {
                    break;
                }
            }
            if (canvasMoves.length === 0) {
                this.canvasMovesMap.delete(id);
            }
        });
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasMovesMap.size);
        this.canvasMovesMap.forEach((canvasMoves, id) => {
            writer.writeInt16(id);
            writer.writeUint8(canvasMoves.length);
            canvasMoves.forEach((canvasMove) => {
                canvasMove.serialize(writer);
            });
        });
    }

    static create() {
        let object = Object.create(this.prototype);
        object.canvasUpdateType = CanvasUpdateType.MOVE;
        object.canvasMovesMap = new Map();
        object.time = 0;
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
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.canvasObject = CanvasObject.create(canvasObject);
        return object;
    }
}