import ServerMessage from "../ServerMessage.js";
import ServerMessageType from "../ServerMessageType.js";
import {Canvas} from "../../../canvas/Canvas.js";
import ByteBuffer from "../../ByteBuffer.js";

export default class ServerMessageOpenDocument extends ServerMessage {
    id: bigint;
    canvas: Canvas;

    constructor(byteBuffer: ByteBuffer) {
        super(ServerMessageType.OPEN_DOCUMENT);
        this.id = byteBuffer.readBigInt64();
        this.canvas = new Canvas(byteBuffer);
    }
}