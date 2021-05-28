import ApiResponse from "../ApiResponse.js";
import ByteBuffer from "../../../../protocol/ByteBuffer.js";

export default class LoginResponse extends ApiResponse {
    constructor(byteBuffer: ByteBuffer) {
        super();
    }
}