import {activeDocument, getDt, updateCanvas} from "./Document.js";
import {apiUrl} from "./main.js";
import BufferReader from "./protocol/BufferReader.js";
import CanvasObjectWrapper from "./canvas/CanvasObjectWrapper.js";
import {CanvasObjectType} from "./canvas/CanvasObjectType.js";
import CanvasImage from "./canvas/canvasObjects/CanvasImage.js";
const fileUploadButton = document.getElementById("fileUploadButton");

fileUploadButton.addEventListener('change', (event) => {
    newFile(event.target.files[0]);
}, false);

function newFile(file) {
    let fileReader = new FileReader();
    fileReader.addEventListener('load', (event) => {
        //todo verify client/server side that mime type is good
        let request = new XMLHttpRequest();
        request.responseType = 'arraybuffer';
        request.open('POST', apiUrl + '/document/' + activeDocument.id);
        request.setRequestHeader('content-type', file.type);

        request.addEventListener('load', (event) => {
            if (request.status !== 200) {
                console.error(request.status + ' while fetching image id');
                return;
            }
            let id = new BufferReader(new Uint8Array(request.response).buffer).readBigInt64();
            let image = document.createElement('img');
            image.file = file;//server should have the image but we could instead just use the local one
            let object = CanvasImage.create(0, 0, id, image);
            let canvasId = (Math.random() - 0.5) * 32000;
            updateCanvas.update(CanvasObjectType.IMAGE, canvasId, CanvasObjectWrapper.create(getDt(), object));
            activeDocument.canvas.insert(CanvasObjectType.IMAGE, canvasId, object);
        });

        request.send(fileReader.result);
    });

    fileReader.readAsArrayBuffer(file);
}