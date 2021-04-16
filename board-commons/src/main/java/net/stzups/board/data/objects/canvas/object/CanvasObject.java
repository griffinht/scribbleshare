package net.stzups.board.data.objects.canvas.object;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.objects.CanvasImage;
import net.stzups.board.data.objects.canvas.object.objects.Shape;

public class CanvasObject {
    private final short x;
    private final short y;

    public CanvasObject(ByteBuf byteBuf) {
        x = byteBuf.readShort();
        y = byteBuf.readShort();
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort(x);
        byteBuf.writeShort(y);
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
