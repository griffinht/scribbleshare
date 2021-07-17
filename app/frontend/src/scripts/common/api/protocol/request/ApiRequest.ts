import ByteBuffer from "../../../protocol/ByteBuffer.js";

export default abstract class ApiRequest {
    abstract serialize(byteBuffer: ByteBuffer): void;
}