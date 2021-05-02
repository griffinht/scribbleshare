import {ctx, lerp} from "../Canvas.js";
import ByteBuffer from "../../protocol/ByteBuffer";

export default class CanvasObject {
    x: number;
    y: number;
    original: CanvasObject | null;

    constructor(byteBuffer: ByteBuffer) {
        this.x = byteBuffer.readInt16();
        this.y = byteBuffer.readInt16();
        this.original = null;
    }

    lerp(target: CanvasObject, t: number) {
        // @ts-ignore todo
        this.x = lerp(this.original.x, target.x, t);
        // @ts-ignore
        this.y = lerp(this.original.y, target.y, t);
    }

    draw() {
        ctx.fillRect(0, 0, 10, 10);
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeInt16(this.x);
        byteBuffer.writeInt16(this.y);
    }

    static clone(canvasObject: CanvasObject) {
        let object = Object.create(this.prototype);
        object.x = canvasObject.x;
        object.y = canvasObject.y;
        return object;
    }
}
