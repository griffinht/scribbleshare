import ByteBuffer from "../../../../protocol/ByteBuffer.js";
import ApiRequest from "../ApiRequest.js"

export default class LoginRequest extends ApiRequest {
    username: string;
    password: string;
    remember: boolean;

    constructor(form: HTMLFormElement) {
        super();
        this.username = form.username.value;
        this.password = form.password.value;
        this.remember = form.remember.value === "on";
    }

    serialize(byteBuffer: ByteBuffer): void {
        byteBuffer.writeString8(this.username);
        byteBuffer.writeString8(this.password);
        byteBuffer.writeBoolean(this.remember);
    }
}