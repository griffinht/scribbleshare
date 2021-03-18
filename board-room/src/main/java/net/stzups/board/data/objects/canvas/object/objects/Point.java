package net.stzups.board.data.objects.canvas.object.objects;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObject;

public class Point extends CanvasObject {

    public Point(ByteBuf byteBuf) {
        super(byteBuf);
    }
}
