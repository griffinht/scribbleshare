package net.stzups.scribbleshare.data.objects.canvas.canvasObject;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.util.DebugString;

public class EntityCanvasObject extends CanvasObject {
    private final short width;
    private final short height;
    private final byte rotation;

    public EntityCanvasObject(ByteBuf byteBuf) {
        super(byteBuf);
        width = byteBuf.readShort();
        height = byteBuf.readShort();
        rotation = byteBuf.readByte();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(width);
        byteBuf.writeShort(height);
        byteBuf.writeByte(rotation);
    }

    @Override
    public String toString() {
        return DebugString.get(EntityCanvasObject.class, super.toString())
                .add("width", width)
                .add("height", height)
                .add("rotation", rotation)
                .toString();
    }
}
