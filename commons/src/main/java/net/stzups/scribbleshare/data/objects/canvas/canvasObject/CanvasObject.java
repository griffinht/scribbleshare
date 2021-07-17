package net.stzups.scribbleshare.data.objects.canvas.canvasObject;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.CanvasImage;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.CanvasMouse;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.Line;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.Shape;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.util.DebugString;

public class CanvasObject {//todo make abstract?
    private short x;
    private short y;

    public CanvasObject(ByteBuf byteBuf) {
        x = byteBuf.readShort();
        y = byteBuf.readShort();
    }

    public CanvasObjectType getCanvasObjectType() {//this needs to be overridden
        throw new UnsupportedOperationException("Tried to get type of base CanvasObject");
    }

    public void update(CanvasObject canvasObject) {
        this.x = canvasObject.x;
        this.y = canvasObject.y;
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort(x);
        byteBuf.writeShort(y);
    }

    @Override
    public String toString() {
        return DebugString.get(CanvasMouse.class)
                .add("x", x)
                .add("y", y)
                .toString();
    }

    public static CanvasObject deserialize(CanvasObjectType type, ByteBuf byteBuf) throws DeserializationException {
        CanvasObject canvasObject;
        switch (type) {
            case SHAPE:
                canvasObject = new Shape(byteBuf);
                break;
            case IMAGE:
                canvasObject = new CanvasImage(byteBuf);
                break;
            case MOUSE:
                canvasObject = new CanvasMouse(byteBuf);
                break;
            case LINE:
                canvasObject = new Line(byteBuf);
                break;
            default:
                canvasObject = null;
        }
        return canvasObject;
    }
}
