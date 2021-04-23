import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import {getCanvasObject} from "../../canvasObject/getCanvasObject.js";
import CanvasObjectWrapper from "../../canvasObject/CanvasObjectWrapper.js";

export default class CanvasUpdateInsert extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.INSERT);
        this.canvasInsertsMap = new Map();
        this.lastDt = 0;
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

    isDirty() {
        return this.canvasInsertsMap.size > 0;
    }

    clear() {
        this.canvasInsertsMap.clear();
    }


    insert(canvasObjectType, dt, id, canvasObject) {
        let canvasInserts = this.canvasInsertsMap.get(canvasObjectType);
        if (canvasInserts === undefined) {
            canvasInserts = [];
            this.canvasInsertsMap.set(canvasObjectType, canvasInserts);
        }
        let canvasInsert = CanvasInsert.create(dt, id, canvasObject);
        if (canvasInserts.length > 0) {
            canvasInsert.dt -= this.lastDt;
        }
        this.lastDt = dt;
        canvasInserts.push(canvasInsert);
    }

    draw(canvas, dt) {
        this.canvasInsertsMap.forEach((canvasInserts, canvasObjectType) => {

            while (canvasInserts.length > 0) {
                if (canvasInserts[0].dt <= dt) {
                    canvas.canvasObjectWrappers.set(canvasInserts[0].id, new CanvasObjectWrapper(canvasObjectType, canvasInserts[0].canvasObject));
                    if (canvasInserts.length >= 2) {
                        canvasInserts[1].dt += canvasInserts[0].dt;
                    }
                    canvasInserts.shift();
                } else {
                    break;
                }
            }
            if (canvasInserts.length === 0) {
                this.canvasInsertsMap.delete(canvasObjectType);
            }
        });
    }

    serialize(writer) {
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

    static create() {
        let object = Object.create(this.prototype);
        object.canvasUpdateType = CanvasUpdateType.INSERT;
        object.canvasInsertsMap = new Map();
        return object;
    }
}

class CanvasInsert {
    constructor(canvasObjectType, reader) {
        this.dt = reader.readUint8();
        this.id = reader.readInt16();
        this.canvasObject = getCanvasObject(canvasObjectType, reader);
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.id);
        this.canvasObject.serialize(writer);
    }

    static create(dt, id, canvasObject) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.id = id;
        object.canvasObject = canvasObject;
        return object;
    }
}