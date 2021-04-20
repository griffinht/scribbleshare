package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObject;

public class CanvasMove {
    private final byte dt;
    private final CanvasObject canvasObject;

    public CanvasMove(ByteBuf byteBuf) {
        this.dt = byteBuf.readByte();
        this.canvasObject = new CanvasObject(byteBuf);
    }

    CanvasObject getCanvasObject() {
        return canvasObject;
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(dt);
        canvasObject.serialize(byteBuf);
    }
}