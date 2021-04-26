import {ShapeType} from "./canvas/canvasObject/canvasObjects/Shape.js";

let shape = {a:ShapeType.RECTANGLE};

const out = document.getElementById("shapeOut");
document.getElementById("shape1").addEventListener('click', (event) => {
    out.src = "assets/square.png";
    shape.a = ShapeType.RECTANGLE;
})
document.getElementById("shape2").addEventListener('click', (event) => {
    out.src = "assets/circle.png";
    shape.a = ShapeType.ELLIPSE;
})
document.getElementById("shape3").addEventListener('click', (event) => {
    out.src = "assets/triangle.png";
    shape.a = ShapeType.TRIANGLE;
})
export default shape;