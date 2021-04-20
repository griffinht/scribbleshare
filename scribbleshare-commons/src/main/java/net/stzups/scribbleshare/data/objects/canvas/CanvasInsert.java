package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;

public class CanvasInsert {
    private final byte dt;
    private final short id;
    private final CanvasObject canvasObject;

    public CanvasInsert(CanvasObjectType type, ByteBuf byteBuf) {
        this.dt = byteBuf.readByte();
        this.id = byteBuf.readShort();
        this.canvasObject = CanvasObject.getCanvasObject(type, byteBuf);
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(dt);
        byteBuf.writeShort(id);
        canvasObject.serialize(byteBuf);
    }

    public short getId() {
        return id;
    }

    CanvasObject getCanvasObject() {
        return canvasObject;
    }
}