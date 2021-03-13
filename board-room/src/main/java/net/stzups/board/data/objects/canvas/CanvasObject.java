package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;

public class CanvasObject {
    private CanvasObjectType type;

    CanvasObject(CanvasObjectType type) {
        this.type = type;
    }

    /**
     * Subclasses need to call this method
     */
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) type.getId());
    }
}
