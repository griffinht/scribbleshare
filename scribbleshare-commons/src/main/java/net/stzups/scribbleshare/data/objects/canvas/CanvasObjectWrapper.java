package net.stzups.scribbleshare.data.objects.canvas;

import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;

class CanvasObjectWrapper {
    private final CanvasObjectType type;
    private final CanvasObject canvasObject;

    CanvasObjectWrapper(CanvasObjectType type, CanvasObject canvasObject) {
        this.type = type;
        this.canvasObject = canvasObject;
    }

    CanvasObjectType getType() {
        return type;
    }

    CanvasObject getCanvasObject() {
        return canvasObject;
    }
}
