document.getElementById('logout')!.addEventListener('submit', (event) => {
    let request = new XMLHttpRequest();
    request.open('POST', '/logout');
    request.send();
    event.preventDefault();
});