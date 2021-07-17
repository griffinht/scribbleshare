import api from "./common/api/Api.js"
import RegisterRequest from "./common/api/protocol/request/requests/RegisterRequest.js";
import RegisterResponse, {RegisterResponseResult} from "./common/api/protocol/response/responses/RegisterResponse.js";

document.getElementById('register')!.addEventListener('submit', (event) => {
    console.log(event);
    event.preventDefault();
    api.register(new RegisterRequest(event.target as HTMLFormElement))
        .then((registerResponse: RegisterResponse) => {
            switch (registerResponse.result) {
                case RegisterResponseResult.USERNAME_TAKEN:
                    window.alert('Username already taken');
                    break;
                case RegisterResponseResult.SUCCESS:
                    window.location.href = '/login';
                    break;
                default:
                    throw new Error('Unknown response ' + registerResponse);
            }
        })
        .catch((e: number) => {
            window.alert('Unknown error ' + e);
        });
});