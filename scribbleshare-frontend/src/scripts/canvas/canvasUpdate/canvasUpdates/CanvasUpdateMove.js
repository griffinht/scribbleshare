import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import CanvasObject from "../../canvasObject/CanvasObject.js";

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

    update(canvas) {
        this.canvasMovesMap.forEach((value, id) => {
            let canvasMoves = canvas.canvasMovesMap.get(id);
            if (canvasMoves === undefined) {
                canvas.canvasMovesMap.set(id, value);
            } else {
                value.forEach(canvasMove => {
                    canvasMoves.push(canvasMove);
                });
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

    static create(canvasMovesMap) {
        let object = Object.create(this.prototype);
        object.canvasMovesMap = canvasMovesMap;
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