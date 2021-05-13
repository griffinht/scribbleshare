import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {Canvas} from "../../../canvas/Canvas.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageOpenDocument extends ServerMessage {
    id: bigint;
    canvas: Canvas;

    constructor(byteBuffer: ByteBuffer) {
        super();
        this.id = byteBuffer.readBigInt64();
        this.canvas = new Canvas(byteBuffer);
    }

    getType(): ServerMessageType {
        return ServerMessageType.OPEN_DOCUMENT;
    }
}