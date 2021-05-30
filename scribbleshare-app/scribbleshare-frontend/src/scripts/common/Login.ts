import Modal from "./Modal.js";


/*
api.register(new RegisterRequest("asdasd", "asdasd"))
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
    });*/
let modal = new Modal(document.getElementById("loginModal")!);
modal.show();
/*
api.login(new LoginRequest("test", "test", true))
    .then((loginResponse : LoginResponse | void) => {
        console.log(loginResponse, 'login success');
    })
    .catch((responseError: ResponseError) => {
        console.log(responseError);
    });*/
