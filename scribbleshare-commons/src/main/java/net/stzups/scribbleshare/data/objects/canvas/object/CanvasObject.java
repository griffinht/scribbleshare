package net.stzups.scribbleshare.data.objects.canvas.object;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.object.canvasObjects.CanvasImage;
import net.stzups.scribbleshare.data.objects.canvas.object.canvasObjects.Shape;

public class CanvasObject {
    private short x;
    private short y;
    private short width;
    private short height;
    private short rotation;

    public CanvasObject(ByteBuf byteBuf) {
        x = byteBuf.readShort();
        y = byteBuf.readShort();
        width = byteBuf.readShort();
        height = byteBuf.readShort();
        rotation = byteBuf.readByte();
    }

    public void update(CanvasObject canvasObject) {
        this.x = canvasObject.x;
        this.y = canvasObject.y;
        this.width = canvasObject.width;
        this.height = canvasObject.height;
        this.rotation = canvasObject.rotation;
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort(x);
        byteBuf.writeShort(y);
        byteBuf.writeShort(width);
        byteBuf.writeShort(height);
        byteBuf.writeByte(rotation);
    }

    public static CanvasObject getCanvasObject(CanvasObjectType canvasObjectType, ByteBuf byteBuf) {
        CanvasObject canvasObject;
        switch (canvasObjectType) {
            case SHAPE:
                canvasObject = new Shape(byteBuf);
                break;
            case IMAGE:
                canvasObject = new CanvasImage(byteBuf);
                break;
            default:
                canvasObject = null;
        }
        return canvasObject;
    }
}
