import {activeDocument, getDt, updateCanvas} from "./Document.js";
import {apiUrl} from "./main.js";
import BufferReader from "./protocol/BufferReader.js";
import CanvasObjectWrapper from "./canvas/CanvasObjectWrapper.js";
import {CanvasObjectType} from "./canvas/CanvasObjectType.js";
import CanvasImage from "./canvas/canvasObjects/CanvasImage.js";
import BufferWriter from "./protocol/BufferWriter.js";

const fileUploadButton = document.getElementById("fileUploadButton");

fileUploadButton.addEventListener('change', (event) => {
    uploadImage(event.target.files[0]);
}, false);

function uploadImage(file) {
    let fileReader = new FileReader();
    fileReader.addEventListener('load', (event) => {
        // draw image file to canvas
        let image = document.createElement('img');
        image.src = fileReader.result;
        image.addEventListener('load', (event) => {
            let canvas = document.createElement('canvas');
            canvas.height = image.height;
            canvas.width = image.width;
            let ctx = canvas.getContext('2d');
            ctx.drawImage(image, 0, 0);
            //todo resize

            // re encode image to smallest format
            let png = canvas.toDataURL('image/png');
            let jpeg = canvas.toDataURL('image/jpeg', 0.69); // very nice quality
            let output;
            if (png.length < jpeg.length) {
                output = png;
            } else {
                output = jpeg;
            }

            let request = new XMLHttpRequest();
            request.responseType = 'arraybuffer';
            request.open('POST', apiUrl + '/document/' + activeDocument.id);
            console.log(output);

            let dataIndex = output.indexOf('data:') + 5;
            console.log(output.substring(dataIndex, output.indexOf(';', dataIndex)));
            request.setRequestHeader('content-type', output.substring(dataIndex, output.indexOf(';', dataIndex)));

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

            let writer = new BufferWriter();
            writer.writeBase64_32(output.substr(output.indexOf('base64,') + 7));
            request.send(writer.getBuffer());
        });
        //todo verify client/server side that mime type is good
    });

    //fileReader.readAsArrayBuffer(file);
    fileReader.readAsDataURL(file)
}