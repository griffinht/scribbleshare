document.getElementById("logout")!.addEventListener('submit', (event) => {
    console.log(event);
    let request = new XMLHttpRequest();
    request.open("POST", "");
    event.preventDefault();
});