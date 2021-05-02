import {activeDocument} from "./Document.js";
import CanvasImage from "./canvas/canvasObject/canvasObjects/CanvasImage.js";
import Modal from "./Modal.js";
import Environment from "./Environment.js";
import ByteBuffer from "./protocol/ByteBuffer.js";

const MAX_WIDTH = 1280;
const MAX_HEIGHT = 1280;
const JPEG_QUALITY = 0.69; // very nice quality with a relatively small file size

const fileUploadModal = new Modal(document.getElementById("fileUploadModal")!);

document.body.addEventListener('dragover', (event) => {
    if (activeDocument !== null
        && event.dataTransfer !== null
        && event.dataTransfer.items.length > 0
        && event.dataTransfer.items[0].kind === 'file') {
        fileUploadModal.show();
    } else {
        fileUploadModal.hide();
    }
    event.preventDefault();
});

document.body.addEventListener('dragleave', (event) => {
    fileUploadModal.hide();
    console.log('sfd');
    event.preventDefault();
})

document.body.addEventListener('drop', (event) => {
    console.log(event);
    if (activeDocument === null || event.dataTransfer === null || event.dataTransfer.files.length === 0) {
        return;
    }
    event.preventDefault();
    let file = event.dataTransfer.files[0];

    fileUploadModal.modal.innerText = 'Uploading ' + file.name + '...';

    let fileReader = new FileReader();
    fileReader.addEventListener('load', (event) => {
        // draw image file to canvas
        let image = document.createElement('img')! as HTMLImageElement;
        if (fileReader.result === null || fileReader.result instanceof ArrayBuffer) {
            return;
        }
        image.src = fileReader.result;
        image.addEventListener('load', (event) => {
            let canvas = document.createElement('canvas');
            // resize image if it is too large
            let scale = Math.min(
                MAX_WIDTH / Math.max(image.width, MAX_WIDTH),
                MAX_HEIGHT / Math.max(image.height, MAX_HEIGHT));
            // scale will be 1 if the image is not too large
            canvas.width = image.width * scale;
            canvas.height = image.height * scale;
            let ctx = canvas.getContext('2d')!;//todo import from someweher
            ctx.drawImage(image, 0, 0, canvas.width, canvas.height);
            // re encode image to smallest format
            let png = canvas.toDataURL('image/png');
            let jpeg = canvas.toDataURL('image/jpeg', JPEG_QUALITY);
            let output;
            if (png.length < jpeg.length) {
                output = png;
            } else {
                output = jpeg;
            }

            let request = new XMLHttpRequest();
            request.responseType = 'arraybuffer';
            // @ts-ignore todo
            request.open('POST', Environment.API_HOST + '/document/' + activeDocument.id);

            let dataIndex = output.indexOf('data:') + 5;
            request.setRequestHeader('content-type', output.substring(dataIndex, output.indexOf(';', dataIndex)));

            request.addEventListener('load', (event) => {
                fileUploadModal.hide()
                fileUploadModal.modal.innerText = 'Upload file';
                if (request.status !== 200) {
                    console.error(request.status + ' while fetching image id');
                    return;
                }
                let id = new ByteBuffer(new Uint8Array(request.response).buffer).readBigInt64();
                let object = CanvasImage.create(0, 0, id, image);
                // @ts-ignore todo
                activeDocument.canvas.insert(CanvasObjectType.IMAGE, object);
            });

            request.addEventListener('progress', (event) => {
                //todo
            });

            request.send(Uint8Array.from(atob(output.substr(output.indexOf('base64,') + 7)), c => c.charCodeAt(0))); //base64 string to binary
        });
        //todo verify client/server side that mime type is good
    });

    //fileReader.readAsArrayBuffer(file);
    fileReader.readAsDataURL(file);
});
