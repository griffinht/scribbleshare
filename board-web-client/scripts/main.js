console.log('hello');
const socket = new WebSocket('ws://localhost/');

socket.addEventListener('open', function(event) {
    socket.send('whassup');
});

socket.addEventListener('message', function(event) {
    console.log(event.data);
});