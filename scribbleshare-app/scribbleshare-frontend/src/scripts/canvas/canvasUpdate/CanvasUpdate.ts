import ByteBuffer from "../../protocol/ByteBuffer.js";
import {Canvas} from "../Canvas.js";
import CanvasUpdateType from "./CanvasUpdateType.js";
import CanvasUpdateInsert from "./canvasUpdates/CanvasUpdateInsert.js";
import CanvasUpdateMove from "./canvasUpdates/CanvasUpdateMove.js";
import CanvasUpdateDelete from "./canvasUpdates/CanvasUpdateDelete.js";

export default abstract class CanvasUpdate {
    dt: number;

    protected constructor(byteBuffer: ByteBuffer) {
        this.dt = byteBuffer.readUint8();
    }

    draw(canvas: Canvas, dt: number): boolean {
        return true;
    }

    abstract getType(): CanvasUpdateType;

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeUint8(this.getType());
    }

    static deserialize(type: CanvasUpdateType, byteBuffer: ByteBuffer): CanvasUpdate {
        let object: CanvasUpdate;
        switch (type) {
            case CanvasUpdateType.INSERT:
                object = new CanvasUpdateInsert(byteBuffer);
                break;
            case CanvasUpdateType.MOVE:
                object = new CanvasUpdateMove(byteBuffer);
                break;
            case CanvasUpdateType.DELETE:
                object = new CanvasUpdateDelete(byteBuffer);
                break;
            default:
                throw new Error('unknown canvasUpdateType ' + type);
        }
        return object;
    }
}