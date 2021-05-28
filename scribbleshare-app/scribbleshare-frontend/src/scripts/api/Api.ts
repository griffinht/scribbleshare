import ByteBuffer from "../protocol/ByteBuffer.js";
import LoginRequest from "./protocol/request/requests/LoginRequest.js";
import ResponseError from "./protocol/response/error/ResponseError.js"
import LoginResponse from "./protocol/response/responses/LoginResponse.js";
import Environment from "../Environment.js"
import RegisterRequest from "./protocol/request/requests/RegisterRequest.js";
import RegisterResponse from "./protocol/response/responses/RegisterResponse.js";

class Api {
    uri: string;

    constructor(uri: string) {
        this.uri = uri;
    }

    login(loginRequest: LoginRequest): Promise<LoginResponse> {
        return new Promise((resolve, reject) => {
            let request = new XMLHttpRequest();
            request.responseType = 'arraybuffer';
            request.open('POST', this.uri + loginRequest.getRoute());

            request.addEventListener('load', (event) => {
                if (request.status == 200) {
                    resolve(new LoginResponse(new ByteBuffer(request.response)));
                } else {
                    reject(new ResponseError(request.status))
                }
            });

            let byteBuffer = new ByteBuffer();
            loginRequest.serialize(byteBuffer);
            request.send(byteBuffer.getBuffer());
        })
    }

    register(registerRequest: RegisterRequest): Promise<RegisterResponse> {
        return new Promise((resolve, reject) => {
            let request = new XMLHttpRequest();
            request.responseType = 'arraybuffer';
            request.open('POST', this.uri + registerRequest.getRoute());

            request.addEventListener('load', (event) => {
                if (request.status == 200) {
                    resolve(new RegisterResponse(new ByteBuffer(request.response)));
                } else {
                    reject(new ResponseError(request.status))
                }
            });

            let byteBuffer = new ByteBuffer();
            registerRequest.serialize(byteBuffer);
            request.send(byteBuffer.getBuffer());
        })
    }
}

export default new Api(Environment.getApiHost());

