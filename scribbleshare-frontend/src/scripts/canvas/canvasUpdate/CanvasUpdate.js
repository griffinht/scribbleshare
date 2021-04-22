export default class CanvasUpdate {
    constructor(canvasUpdateType) {
        this.canvasUpdateType = canvasUpdateType;
    }

    isDirty() {//todo abstract es6 method?
        return false;
    }

    clear() {

    }

    draw(canvas, dt) {

    }

    serialize(writer) {
        writer.writeUint8(this.canvasUpdateType);
    }
}