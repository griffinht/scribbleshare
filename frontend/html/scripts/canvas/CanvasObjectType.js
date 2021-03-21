import Shape from "./canvasObjects/Shape.js";

export const CanvasObjectType = {
    SHAPE:0,
}

export function getCanvasObject(type, reader) {
    let object;
    switch (type) {
        case CanvasObjectType.SHAPE:
            object = new Shape(reader);
        default:
            console.error('unknown type ' + type);
    }
    return object;
}