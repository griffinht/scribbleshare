package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;

public class CanvasMouse extends CanvasObject {
    public CanvasMouse(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
    }
}
