import api from "./common/api/Api.js"

document.getElementById('logout')!.addEventListener('submit', (event) => {
    event.preventDefault();
    api.logout()
        .then(() => {
            window.location.href = '/';
        })
        .catch((e: number) => {
            window.alert('Unknown error ' + e);
        });

});