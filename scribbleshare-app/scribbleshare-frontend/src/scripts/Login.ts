import api from "./api/Api.js"
import LoginRequest from "./api/protocol/request/requests/LoginRequest.js";
import ResponseError from "./api/protocol/response/error/ResponseError.js";

api.login(new LoginRequest("test", "test", true))
    .catch((requestError: ResponseError) => {
        console.log(requestError);
    })
    .finally(() => {
        console.log(this);
    });