import {ShapeType} from "./canvas/canvasObject/canvasObjects/Shape.js";

let shape = ShapeType.RECTANGLE;

const out: HTMLImageElement = document.getElementById("shapeOut") as HTMLImageElement;
document.getElementById("shape1")!.addEventListener('click', (event) => {
    out.src = "assets/square.png";
    shape = ShapeType.RECTANGLE;
})
document.getElementById("shape2")!.addEventListener('click', (event) => {
    out.src = "assets/circle.png";
    shape = ShapeType.ELLIPSE;
})
document.getElementById("shape3")!.addEventListener('click', (event) => {
    out.src = "assets/triangle.png";
    shape = ShapeType.TRIANGLE;
})
export default shape;