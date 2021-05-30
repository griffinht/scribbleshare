import ByteBuffer from "../protocol/ByteBuffer.js";
import LoginRequest from "./protocol/request/requests/LoginRequest.js";
import Environment from "../Environment.js"
import RegisterRequest from "./protocol/request/requests/RegisterRequest.js";
import LoginResponse from "./protocol/response/responses/LoginResponse.js";
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
            request.open('POST', this.uri + '/login');

            request.addEventListener('load', (event) => {
                if (request.status === 200) {
                    resolve(new LoginResponse(new ByteBuffer(request.response)));
                } else {
                    reject(request.status)
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
            request.open('POST', this.uri + '/register');

            request.addEventListener('load', (event) => {
                if (request.status === 200) {
                    resolve(new RegisterResponse(new ByteBuffer(request.response)));
                } else {
                    reject(request.status)
                }
            });

            let byteBuffer = new ByteBuffer();
            registerRequest.serialize(byteBuffer);
            request.send(byteBuffer.getBuffer());
        })
    }

    logout(): Promise<void> {
        return new Promise((resolve, reject) => {
            let request = new XMLHttpRequest();
            request.responseType = 'arraybuffer'
            request.open('POST', this.uri + '/logout');

            request.addEventListener('load', (event) => {
                if (request.status === 200) {
                    resolve();
                } else {
                    reject(request.status);
                }
            })

            request.send();
        });
    }
}

export default new Api(Environment.getApiHost());

