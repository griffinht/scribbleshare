let color = {
    r:0,
    g:0,
    b:0,
}
export default color;
const output = document.getElementById("colorOut");
function set(color) {
    output.style.backgroundColor = 'rgb(' + color.r + ',' + color.g + ',' + color.b + ')';
}
document.getElementById("color1").addEventListener('click', (event) => {
    color.r = 255;
    color.g = 0;
    color.b = 0;
    set(color);
});

document.getElementById("color2").addEventListener('click', (event) => {
    color.r = 0;
    color.g = 255;
    color.b = 0;set(color);
})


document.getElementById("color3").addEventListener('click', (event) => {
    color.r = 0;
    color.g = 255;
    color.b = 255;set(color);
})



document.getElementById("color4").addEventListener('click', (event) => {
    color.r = 0;
    color.g = 0;
    color.b = 255;set(color);
})

document.getElementById("color5").addEventListener('click', (event) => {
    color.r = 255;
    color.g = 0;
    color.b = 255;set(color);
})

document.getElementById("color6").addEventListener('click', (event) => {
    color.r = 255;
    color.g = 255;
    color.b = 0;set(color);
})



document.getElementById("color7").addEventListener('click', (event) => {
    color.r = 255;
    color.g = 255;
    color.b = 255;set(color);
})


document.getElementById("color8").addEventListener('click', (event) => {
    color.r = 0;
    color.g = 0;
    color.b = 0;set(color);
})

