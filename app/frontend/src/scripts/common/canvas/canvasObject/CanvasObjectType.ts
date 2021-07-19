import ByteBuffer from "../../protocol/ByteBuffer.js";
import Shape from "./canvasObjects/Shape.js";
import CanvasImage from "./canvasObjects/CanvasImage.js";
import CanvasMouse from "./canvasObjects/CanvasMouse.js";
import LineCanvasObject from "./canvasObjects/Line.js";

enum CanvasObjectType {
    SHAPE,
    IMAGE,
    MOUSE,
    LINE,
}
export default CanvasObjectType;

export function getCanvasObject(type: CanvasObjectType, byteBuffer: ByteBuffer) {
    let object;
    switch (type) {
        case CanvasObjectType.SHAPE:
            object = new Shape(byteBuffer);
            break;
        case CanvasObjectType.IMAGE:
            object = new CanvasImage(byteBuffer);
            break;
        case CanvasObjectType.MOUSE:
            object = new CanvasMouse(byteBuffer);
            break;
        case CanvasObjectType.LINE:
            object = new LineCanvasObject(byteBuffer);
            break;
        default:
            throw new Error('unknown canvasObjectType ' + type);
    }
    return object;
}