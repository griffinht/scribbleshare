package net.stzups.scribbleshare.data.objects.canvas;

import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;

public class CanvasObjectWrapper {
    private final CanvasObjectType type;
    private final CanvasObject canvasObject;

    public CanvasObjectWrapper(CanvasObjectType type, CanvasObject canvasObject) {
        this.type = type;
        this.canvasObject = canvasObject;
    }

    CanvasObjectType getType() {
        return type;
    }

    public CanvasObject getCanvasObject() {
        return canvasObject;
    }
}
