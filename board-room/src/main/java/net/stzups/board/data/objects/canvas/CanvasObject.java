package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;

public class CanvasObject {
    private CanvasObjectType type;
    private short id;

    protected CanvasObject(CanvasObjectType type, ByteBuf byteBuf) {
        this.type = type;
        this.id = byteBuf.readShort();
    }

    /**
     * Subclasses need to call this method
     */
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) type.getId());
        byteBuf.writeShort(id);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
