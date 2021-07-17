import ApiRequest from "../ApiRequest.js";
import ByteBuffer from "../../../../protocol/ByteBuffer.js";

export default class RegisterRequest extends ApiRequest {
    username: string;
    password: string;

    constructor(form: HTMLFormElement) {
        super();
        this.username = form.username.value;
        this.password = form.password.value;
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeString8(this.username);
        byteBuffer.writeString8(this.password);
    }
}