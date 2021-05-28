import ByteBuffer from "../../../../protocol/ByteBuffer.js";
import ApiRequest from "../ApiRequest.js"

export default class LoginRequest extends ApiRequest {
    username: string;
    password: string;
    remember: boolean;

    constructor(username: string, password: string, remember: boolean) {
        super();
        this.username = username;
        this.password = password;
        this.remember = remember;
    }

    getRoute(): string {
        return "/login";
    }

    serialize(byteBuffer: ByteBuffer): void {
        byteBuffer.writeString8(this.username);
        byteBuffer.writeString8(this.password);
        byteBuffer.writeBoolean(this.remember);
    }
}