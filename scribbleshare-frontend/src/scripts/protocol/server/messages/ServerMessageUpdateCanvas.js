import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import CanvasObjectWrapper from "../../../canvas/CanvasObjectWrapper.js";

export default class ServerMessageUpdateCanvas extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.UPDATE_CANVAS);
        this.canvasObjectWrappers = new Map();
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            let type = reader.readUint8();
            let map = new Map();
            this.canvasObjectWrappers.set(type, map);
            let lengthJ = reader.readUint16();
            for (let j = 0; j < lengthJ; j++) {
                map.set(reader.readInt16(), new CanvasObjectWrapper(type, reader));
            }
        }
    }
}