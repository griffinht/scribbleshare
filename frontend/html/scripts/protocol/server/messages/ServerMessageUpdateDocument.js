import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";

export default class ServerMessageUpdateDocument extends ServerMessage {
    constructor(reader) {
        super(ServerMessageType.UPDATE_DOCUMENT);
        //todo
        /*let e = {};
        e.id = reader.readInt16();
        let size = reader.readUint16();
        e.points = [];
        for (let i = 0; i < size; i++) {
            let point = {};
            point.dt = reader.readUint8();
            point.x = reader.readInt16();
            point.y = reader.readInt16();
            point.usedDt = 0;
            e.points.push(point);
        }
        this.dispatchEvent('protocol.updateDocument', e);*/
    }
}