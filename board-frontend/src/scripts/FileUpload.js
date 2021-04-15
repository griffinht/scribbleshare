const fileUploadButton = document.getElementById("fileUploadButton");

fileUploadButton.addEventListener('change', (event) => {
    newFile(event.target.files[0]);
}, false);

function newFile(file) {
    let image = document.createElement('img');
    image.file = file;
    document.body.append(image);//todo remove

    let reader = new FileReader();
    reader.onload = (function(img) {
        return function(e) {
            img.src = e.target.result;
        };
    })(image);
    reader.readAsDataURL(file);
}