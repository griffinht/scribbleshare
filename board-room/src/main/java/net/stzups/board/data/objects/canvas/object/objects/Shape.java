package net.stzups.board.data.objects.canvas.object.objects;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObject;

public class Shape extends CanvasObject {
    private int width;
    private int height;

    public Shape(ByteBuf byteBuf) {
        super(byteBuf);
        width = byteBuf.readUnsignedShort();
        height = byteBuf.readUnsignedShort();
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort((short) width);
        byteBuf.writeShort((short) height);
    }
}
