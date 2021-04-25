import {CanvasObjectType} from "./CanvasObjectType.js";
import Shape from "./canvasObjects/Shape.js";
import CanvasImage from "./canvasObjects/CanvasImage.js";
import Mouse from "../../Mouse.js";

export function getCanvasObject(canvasObjectType, reader) {
    let object;
    switch (canvasObjectType) {
        case CanvasObjectType.SHAPE:
            object = new Shape(reader);
            break;
        case CanvasObjectType.IMAGE:
            object = new CanvasImage(reader);
            break;
        case CanvasObjectType.MOUSE:
            object = new Mouse(reader);
            break;
        default:
            console.error('unknown canvasObjectType ' + canvasObjectType);
    }
    return object;
}