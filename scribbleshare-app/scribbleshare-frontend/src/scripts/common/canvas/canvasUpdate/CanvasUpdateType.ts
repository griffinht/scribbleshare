import ByteBuffer from "../../protocol/ByteBuffer.js";
import CanvasUpdate from "./CanvasUpdate.js";
import CanvasUpdateInsert from "./canvasUpdates/CanvasUpdateInsert.js";
import CanvasUpdateMove from "./canvasUpdates/CanvasUpdateMove.js";
import CanvasUpdateDelete from "./canvasUpdates/CanvasUpdateDelete.js";

enum CanvasUpdateType {
    INSERT,
    MOVE,
    DELETE,
}
export default CanvasUpdateType;

export function getCanvasUpdate(canvasUpdateType: CanvasUpdateType, byteBuffer: ByteBuffer): CanvasUpdate {
    let object: CanvasUpdate;
    switch (canvasUpdateType) {
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
            throw new Error('unknown canvasUpdateType ' + canvasUpdateType);
    }
    return object;
}