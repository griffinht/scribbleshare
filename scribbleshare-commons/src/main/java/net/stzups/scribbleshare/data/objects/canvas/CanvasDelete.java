package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;

public class CanvasDelete {
    private final byte dt;
    private final short id;

    public CanvasDelete(ByteBuf byteBuf) {
        this.dt = byteBuf.readByte();
        this.id = byteBuf.readShort();
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(dt);
        byteBuf.writeShort(id);
    }

    short getId() {
        return id;
    }
}