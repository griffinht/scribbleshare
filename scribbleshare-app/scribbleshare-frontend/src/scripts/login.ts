import LoginRequest from "./common/api/protocol/request/requests/LoginRequest.js";
import api from "./common/api/Api.js"
import LoginResponse, {LoginResponseResult} from "./common/api/protocol/response/responses/LoginResponse.js";


document.getElementById('login')!.addEventListener('submit', (event) => {
    event.preventDefault();
    api.login(new LoginRequest(event.target as HTMLFormElement))
        .then((loginResponse: LoginResponse) => {
            switch (loginResponse.result) {
                case LoginResponseResult.FAILED:
                    window.alert('Incorrect username or password');
                    break;
                case LoginResponseResult.SUCCESS:
                    window.location.href = '/';
                    break;
                default:
                    throw new Error('Unknown response ' + loginResponse);
            }
        })
        .catch((e: number) => {
            window.alert('Unknown error ' + e);
        });
});