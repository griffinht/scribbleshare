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
        element.addEventListener('mouseleave', () => {
            this.down = false;
            this.drag = false;
        });
        element.addEventListener('mouseenter', (event) => {
            if (event.buttons === 1) {
                this.down = true;
            }
        });

        element.addEventListener('mousedown', (event) => {
            if (event.buttons !== 1) {
                return;
            }

            this.down = true;
        });
        element.addEventListener('mouseup', (event) => {
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
        element.addEventListener('contextmenu', (event) => {
            event.preventDefault();
            this.dispatchEvent('contextmenu', event);
        });
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