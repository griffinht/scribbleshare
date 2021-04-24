import {CanvasObjectType} from "./CanvasObjectType.js";
import Shape from "./canvasObjects/Shape.js";
import CanvasImage from "./canvasObjects/CanvasImage.js";

export function getCanvasObject(canvasObjectType, reader) {
    let object;
    switch (canvasObjectType) {
        case CanvasObjectType.SHAPE:
            object = new Shape(reader);
            break;
        case CanvasObjectType.IMAGE:
            object = new CanvasImage(reader);
            break;
        default:
            console.error('unknown canvasObjectType ' + canvasObjectType);
    }
    return object;
}