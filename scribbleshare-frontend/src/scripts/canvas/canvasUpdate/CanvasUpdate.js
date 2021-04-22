export default class CanvasUpdate {
    constructor(canvasUpdateType) {
        this.canvasUpdateType = canvasUpdateType;
    }

    update(canvas) {}

    serialize(writer) {
        writer.writeUint8(this.canvasUpdateType);
    }
}