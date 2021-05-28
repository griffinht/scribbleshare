import api from "./api/Api.js"
import LoginRequest from "./api/protocol/request/requests/LoginRequest.js";
import ResponseError from "./api/protocol/response/error/ResponseError.js";
import LoginResponse from "./api/protocol/response/responses/LoginResponse.js";
import RegisterRequest from "./api/protocol/request/requests/RegisterRequest.js";
import RegisterResponse from "./api/protocol/response/responses/RegisterResponse.js";

api.login(new LoginRequest("test", "test", true))
    .then((loginResponse : LoginResponse | void) => {
        console.log(loginResponse, 'login success');
    })
    .catch((responseError: ResponseError) => {
        console.log(responseError);
    });

api.register(new RegisterRequest("asd", "asd"))
    .then((registerResponse: RegisterResponse | void) => {
        console.log(registerResponse);
    })
    .catch((responseError: ResponseError) => {
        switch (responseError.responseCode) {
            case 409:
                console.log('Username already taken')
                break;
            default:
                console.log(responseError)
        }
    });
