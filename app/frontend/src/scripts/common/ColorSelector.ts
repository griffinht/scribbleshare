import Color from "./Color.js";

const color: Color = Color.create(0, 0, 0);
export default color;
const output: HTMLElement = document.getElementById("colorOut")!;
function set(red: number, green: number, blue: number) {
    color.set(red, green, blue);
    output.style.backgroundColor = 'rgb(' + red + ',' + green + ',' + blue + ')';
}
document.getElementById("color1")!.addEventListener('click', (event) => {
    set(255, 0, 0);
});

document.getElementById("color2")!.addEventListener('click', (event) => {
    set(0, 255, 0);
})


document.getElementById("color3")!.addEventListener('click', (event) => {
    set(0, 255, 255);
})



document.getElementById("color4")!.addEventListener('click', (event) => {
    set(0, 0, 255);
})

document.getElementById("color5")!.addEventListener('click', (event) => {
    set(255, 0, 255);
})

document.getElementById("color6")!.addEventListener('click', (event) => {
    set(255, 255, 0);
})



document.getElementById("color7")!.addEventListener('click', (event) => {
    set(255, 255, 255);
})


document.getElementById("color8")!.addEventListener('click', (event) => {
    set(0, 0, 0);
})

