package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;

public class CanvasImage extends CanvasObject {
    private final long id;

    public CanvasImage(ByteBuf byteBuf) {
        super(byteBuf);
        this.id = byteBuf.readLong();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(id);
    }
}