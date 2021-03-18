package net.stzups.board.data.objects.canvas.object.wrappers;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObjectType;

public class CanvasObjectWrapper {
    private CanvasObjectType canvasObjectType;
    private short id;

    public CanvasObjectWrapper(ByteBuf byteBuf) {
        canvasObjectType = CanvasObjectType.valueOf(byteBuf.readUnsignedByte());
        id = byteBuf.readUnsignedByte();
    }

    public CanvasObjectType getCanvasObjectType() {
        return canvasObjectType;
    }

    public short getId() {
        return id;
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((short) canvasObjectType.getId());
        byteBuf.writeShort(id);

    }
}
