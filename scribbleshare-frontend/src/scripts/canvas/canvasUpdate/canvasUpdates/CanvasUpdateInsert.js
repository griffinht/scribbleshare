import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import CanvasObjectWrapper from "../../canvasObject/CanvasObjectWrapper.js";
import {getCanvasObject} from "../../canvasObject/getCanvasObject.js";

export default class CanvasUpdateInsert extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.INSERT);
        this.canvasInsertsMap = new Map();
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            let canvasObjectType = reader.readUint8();
            let canvasInserts = [];
            let lengthJ = reader.readUint8();
            for (let j = 0; j < lengthJ; j++) {
                canvasInserts.push(new CanvasInsert(canvasObjectType, reader));
            }
            this.canvasInsertsMap.set(canvasObjectType, canvasInserts);
        }
    }

    update(canvas) {
        this.canvasInsertsMap.forEach((canvasInserts, canvasObjectType) => {
            canvasInserts.forEach((canvasInsert) => {
                canvas.canvasInserts.push(canvasInsert);
            });
        });
    }

    serialize() {
        super.serialize(writer);
        writer.writeUint8(this.canvasInsertsMap.size);
        this.canvasInsertsMap.forEach((canvasInserts, canvasObjectType) => {
            writer.writeUint8(canvasObjectType);
            writer.writeUint8(canvasInserts.length);
            canvasInserts.forEach((canvasInsert) => {
                canvasInsert.serialize(writer);
            });
        });
    }

    static create(canvasInsertsMap) {
        let object = Object.create(this.prototype);
        object.canvasInsertsMap = canvasInsertsMap;
        return object;
    }
}

class CanvasInsert {
    constructor(canvasObjectType, reader) {
        this.dt = reader.readUint8();
        this.id = reader.readInt16();
        this.canvasObjectWrapper = new CanvasObjectWrapper(canvasObjectType, getCanvasObject(canvasObjectType, reader));
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.id);
        this.canvasObjectWrapper.serialize(writer);
    }

    static create(dt, id, canvasObjectWrapper) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.id = id;
        object.canvasObjectWrapper = canvasObjectWrapper;
        return object;
    }
}