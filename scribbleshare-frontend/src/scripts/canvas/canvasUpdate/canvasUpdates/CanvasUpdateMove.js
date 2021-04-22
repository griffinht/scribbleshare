import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import CanvasObject from "../../canvasObject/CanvasObject.js";
import {ctx} from "../../Canvas.js";

export default class CanvasUpdateMove extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.MOVE);
        this.canvasMovesMap = new Map();
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
    }

    move(id, dt, canvasObject) {
        let canvasMoves = this.canvasMovesMap.get(id);
        if (canvasMoves === undefined) {
            return;
        }

        canvasMoves.push(CanvasMove.create(dt, canvasObject));
    }

    draw(canvas, dt) {
        this.canvasMovesMap.forEach((canvasMoves, id) => {
            let canvasObjectWrapper = canvas.canvasObjectWrappers.get(id);
            if (canvasObjectWrapper === undefined) {
                this.clear();
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
        object.canvasMovesMap = new Map();
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
        object.canvasObject = canvasObject;
        return object;
    }
}