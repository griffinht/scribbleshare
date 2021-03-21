import {ctx} from "./Canvas.js";

export default class CanvasObject {
    constructor(props) {
        this.x = props.readInt16();
        this.y = props.readInt16();
    }

    draw() {
        ctx.fillRect(this.x, this.y, 10, 10);
    }

    serialize(writer) {
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
    }
}