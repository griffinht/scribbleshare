import CanvasUpdate from "../CanvasUpdate.js";
import {CanvasUpdateType} from "../CanvasUpdateType.js";
import {activeDocument} from "../../../Document.js";

export default class CanvasUpdateMouseMove extends CanvasUpdate {
    constructor(reader) {
        super(CanvasUpdateType.MOUSEMOVE);
        this.time = 0;
        this.client = activeDocument.clients.get(reader.readInt16());
        this.mouseMoves = [];
        let length = reader.readUint8();
        for (let i = 0; i < length; i++) {
            let mouseMoves = [];
            let lengthJ = reader.readUint8();
            for (let j = 0; j < lengthJ; j++) {
                mouseMoves.push(new MouseMove(reader));
            }
            this.mouseMoves.push(mouseMoves);
        }
    }

    isDirty() {
        return this.mouseMoves.length > 0;
    }

    move(client, time, x, y) {
        let mouseMoves = this.mouseMovesMap.get(client);
        if (mouseMoves === undefined) {
            mouseMoves = [];
            this.mouseMovesMap.set(client, mouseMoves);
        }
        mouseMoves.push(MouseMove.create(time - this.time, x, y));
        this.time = time;
    }

    draw(canvas, dt) {
        this.time += dt; //accumulated dt
        while(this.mouseMoves.length > 0) {
            while (this.mouseMoves[0].length > 0) {
                while (this.mouseMoves[0][0].mouseMoves.length > 0) {
                    if (this.mouseMoves[0][0].dt <= this.time) {
                        this.time -= this.mouseMoves[0][0].dt;
                        this.client.mouseX = this.mouseMoves[0][0].x;
                        this.client.mouseY = this.mouseMoves[0][0].y;
                        this.mouseMoves[0].shift();
                    } else {
                        break;
                    }
                }
                if (this.mouseMoves[0][0].length === 0) {
                    this.mouseMoves[0].shift();
                }
            }
            if (this.mouseMoves[0].length === 0) {
                this.mouseMoves.shift();
            }
        }
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeInt16(this.client.id);
        writer.writeUint8(this.mouseMoves.length);
        this.mouseMoves.forEach((mouseMoves) => {
            writer.writeUint8(mouseMoves.length);
            mouseMoves.forEach((mouseMove) => {
                mouseMove.serialize(writer);
            });
        });
    }

    static create(client) {
        let object = Object.create(this.prototype);
        object.canvasUpdateType = CanvasUpdateType.MOUSEMOVE;
        object.mouseMoves = [];
        object.time = 0;
        this.client = client;
        return object;
    }
}

class MouseMove {
    constructor(reader) {
        this.dt = reader.readUint8();
        this.x = reader.readInt16();
        this.y = reader.readInt16();
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
    }

    static create(dt, x, y) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.x = x;
        object.y = y;
        return object;
    }
}