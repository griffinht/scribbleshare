package net.stzups.board.data.objects.canvas.object.objects;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObject;

public class Shape extends CanvasObject {
    private int length;
    private int width;

    public Shape(ByteBuf byteBuf) {
        super(byteBuf);
        length = byteBuf.readUnsignedShort();
        width = byteBuf.readUnsignedShort();
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort((short) length);
        byteBuf.writeShort((short) width);
    }
}
