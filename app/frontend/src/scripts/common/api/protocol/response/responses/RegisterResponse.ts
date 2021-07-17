import ByteBuffer from "../../../../protocol/ByteBuffer.js";

export default class RegisterResponse {
    result: RegisterResponseResult;

    constructor(byteBuffer: ByteBuffer) {
        this.result = byteBuffer.readUint8();
    }
}

export enum RegisterResponseResult {
    SUCCESS,
    USERNAME_TAKEN
}