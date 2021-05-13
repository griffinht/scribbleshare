export default class Mouse {
    down: boolean = false;
    drag: boolean = false;

    x: number = 0;
    y: number = 0;
    dx: number = 0;
    dy: number = 0;
    width: number = 0;
    height: number = 0;

    eventListeners: Map<string, Array<(event: MouseEvent) => void>> = new Map();

    constructor(element: HTMLElement) {
        element.addEventListener('mouseenter', (event) => {
            this.mousedown(event);
        });
        element.addEventListener('mousedown', (event) => {
            this.mousedown(event);
        });
        element.addEventListener('mousemove', (event) => {
            this.x = event.offsetX;//todo left click drag only
            this.y = event.offsetY;
            this.dx += event.movementX;
            this.dy += event.movementY;
            if (!this.down) {
                return;
            }

            if (this.drag) {
                this.dispatchEvent('drag', event);
            } else {
                this.drag = true;
                this.dispatchEvent('dragstart', event);
            }
        });
        element.addEventListener('mouseleave', (event) => {
            this.mouseup(event);
        });
        element.addEventListener('mouseup', (event) => {
            this.mouseup(event);
        });
        element.addEventListener('contextmenu', (event) => {
            event.preventDefault();
            this.dispatchEvent('contextmenu', event);
        });
    }

    mouseup(event: MouseEvent) {
        if (!this.down) {
            return;
        }

        this.down = false;
        if (this.drag) {
            this.drag = false;
            this.dispatchEvent('dragstop', event);
        } else {
            this.dispatchEvent('click', event);
        }
    }

    mousedown(event: MouseEvent) {
        if (event.buttons !== 1) {
            return;
        }

        this.down = true;
    }

    addEventListener(type: string, listener: (event: MouseEvent) => void) {//todo better typescript string key stuff idk
        let array = this.eventListeners.get(type);
        if (array === undefined) {
            array = [];
            this.eventListeners.set(type, array);
        }
        array.push(listener);
    }

    dispatchEvent(type: string, event: MouseEvent) {
        let array = this.eventListeners.get(type);
        if (array === undefined) {
            return;
        }

        array.forEach((listener) => listener(event));
    }

    reset() {
        this.dx = 0;
        this.dy = 0;
    }
}