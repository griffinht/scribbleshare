import {CanvasObjectType} from "../CanvasObjectType.js";
import CanvasObject from "../CanvasObject.js";


export default class Points extends CanvasObject {
    constructor(reader) {
        super(CanvasObjectType.POINTS, reader);
        this.points = [];
        for (let i = 0; i < reader.readUint16(); i++) {
            this.points[i] = {
                dt:reader.readUint8(),
                x:reader.readUint16(),
                y:reader.readUint16()
            };
        }
    }

    draw(dt) {
        super.draw(dt);

    }

    serialize(writer) {
        super.serialize(writer);
    }
}