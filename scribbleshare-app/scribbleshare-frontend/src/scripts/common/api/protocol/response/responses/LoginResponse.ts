import ByteBuffer from "../../../../protocol/ByteBuffer.js";

export default class LoginResponse {
    result: LoginResponseResult;

    constructor(byteBuffer: ByteBuffer) {
        this.result = byteBuffer.readUint8();
    }
}

export enum LoginResponseResult {
    SUCCESS,
    FAILED
}