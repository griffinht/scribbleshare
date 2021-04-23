import {CanvasUpdateType} from "./CanvasUpdateType.js";
import CanvasUpdateInsert from "./canvasUpdates/CanvasUpdateInsert.js";
import CanvasUpdateMove from "./canvasUpdates/CanvasUpdateMove.js";
import CanvasUpdateDelete from "./canvasUpdates/CanvasUpdateDelete.js";

export function getCanvasUpdate(canvasUpdateType, reader) {
    let object;
    switch (canvasUpdateType) {
        case CanvasUpdateType.INSERT:
            object = new CanvasUpdateInsert(reader);
            break;
        case CanvasUpdateType.MOVE:
            object = new CanvasUpdateMove(reader);
            break;
        case CanvasUpdateType.DELETE:
            object = new CanvasUpdateDelete(reader);
            break;
        default:
            console.error('unknown canvasUpdateType ' + canvasUpdateType);
    }
    return object;
}