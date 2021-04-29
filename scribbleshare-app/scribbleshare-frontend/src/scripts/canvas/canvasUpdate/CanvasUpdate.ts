export default class CanvasUpdate {
    constructor(canvasUpdateType) {
        this.canvasUpdateType = canvasUpdateType;
    }

    draw(canvas, dt) {

    }

    serialize(writer) {
        writer.writeUint8(this.canvasUpdateType);
    }
}