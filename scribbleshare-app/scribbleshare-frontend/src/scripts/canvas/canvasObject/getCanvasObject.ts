import {CanvasObjectType} from "./CanvasObjectType.js";
import Shape from "./canvasObjects/Shape.js";
import CanvasImage from "./canvasObjects/CanvasImage.js";
import CanvasMouse from "./canvasObjects/CanvasMouse.js";
import LineCanvasObject from "./canvasObjects/Line.js";

export function getCanvasObject(canvasObjectType, byteBuffer) {
    let object;
    switch (canvasObjectType) {
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
            console.error('unknown canvasObjectType ' + canvasObjectType);
    }
    return object;
}