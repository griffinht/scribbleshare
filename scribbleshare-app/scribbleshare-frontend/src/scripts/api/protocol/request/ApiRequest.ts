import ByteBuffer from "../../../protocol/ByteBuffer.js";

export default abstract class ApiRequest {
    abstract getRoute(): string;
    abstract serialize(byteBuffer: ByteBuffer): void;
}