import ApiRequest from "../ApiRequest.js";
import ByteBuffer from "../../../../protocol/ByteBuffer.js";

export default class RegisterRequest extends ApiRequest {
    username: string;
    password: string;

    constructor(username: string, password: string) {
        super();
        this.username = username;
        this.password = password;
    }

    getRoute(): string {
        return "/register";
    }

    serialize(byteBuffer: ByteBuffer) {
        byteBuffer.writeString8(this.username);
        byteBuffer.writeString8(this.password);
    }
}