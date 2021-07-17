package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.util.DebugString;

public class CanvasMouse extends CanvasObject {
    public CanvasMouse(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public CanvasObjectType getCanvasObjectType() {
        return CanvasObjectType.MOUSE;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
    }

    @Override
    public String toString() {
        return DebugString.get(CanvasMouse.class, super.toString())
                .toString();
    }
}
