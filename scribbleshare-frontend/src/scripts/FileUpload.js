import {activeDocument, getDt, updateCanvas} from "./Document.js";
import {CanvasObjectType} from "./canvas/CanvasObjectType.js";
import CanvasObjectWrapper from "./canvas/CanvasObjectWrapper.js";
import CanvasImage from "./canvas/canvasObjects/CanvasImage.js";
import {apiUrl} from "./main.js";
import BufferWriter from "./protocol/BufferWriter.js";
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

    let object = CanvasImage.create(0, 0, image);
    let id = (Math.random() - 0.5) * 32000;
    updateCanvas.update(CanvasObjectType.IMAGE, id, CanvasObjectWrapper.create(getDt(), object));
    activeDocument.canvas.insert(CanvasObjectType.IMAGE, id, object);

    let request = new XMLHttpRequest();
    request.open('POST', apiUrl + '/resources/' + activeDocument.id + '/' + object.id);
    let writer = new BufferWriter();
    writer.writeBase64(object.image.src.substr(this.image.src.indexOf(',') + 1))
    request.send(writer.getBuffer());
}