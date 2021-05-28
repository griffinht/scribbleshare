import ApiResponse from "../ApiResponse.js";
import ByteBuffer from "../../../../protocol/ByteBuffer.js";

export default class RegisterResponse extends ApiResponse {
    constructor(byteBuffer: ByteBuffer) {
        super();
    }
}