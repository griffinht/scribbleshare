import CanvasUpdateInsert from "./canvasUpdates/CanvasUpdateInsert.js";
import CanvasUpdateMove from "./canvasUpdates/CanvasUpdateMove.js";
import CanvasUpdateDelete from "./canvasUpdates/CanvasUpdateDelete.js";
import ByteBuffer from "../../protocol/ByteBuffer";
import CanvasUpdate from "./CanvasUpdate";

export function getCanvasUpdate(type: CanvasUpdateType, byteBuffer: ByteBuffer): CanvasUpdate {
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