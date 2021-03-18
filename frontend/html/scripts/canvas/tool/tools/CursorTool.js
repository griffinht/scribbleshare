import Tool from "../Tool.js";

export default class CursorTool extends Tool {
    constructor() {
        super();
    }

    onEvent(document, event) {
        switch (event.type) {
            case 'click':
                console.log('cursor clicked', event);
                break;
        }
    }
}