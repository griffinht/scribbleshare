import Tool from "../Tool.js";

export default class ShapeTool extends Tool {
    constructor() {
        super();
    }

    onEvent(document, event) {
        switch (event.type) {
            case 'click':
                document.canvas.addObject(new Shape());
                break;
        }
    }
}