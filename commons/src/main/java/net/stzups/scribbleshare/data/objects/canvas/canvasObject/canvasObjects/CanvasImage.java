package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.EntityCanvasObject;
import net.stzups.util.DebugString;

public class CanvasImage extends EntityCanvasObject {
    private final long id;

    public CanvasImage(ByteBuf byteBuf) {
        super(byteBuf);
        this.id = byteBuf.readLong();
    }

    @Override
    public CanvasObjectType getCanvasObjectType() {
        return CanvasObjectType.IMAGE;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(id);
    }

    @Override
    public String toString() {
        return DebugString.get(CanvasImage.class, super.toString())
                .add("id", id)
                .toString();
    }
}
