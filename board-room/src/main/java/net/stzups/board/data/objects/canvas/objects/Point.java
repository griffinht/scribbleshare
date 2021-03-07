package net.stzups.board.data.objects.canvas.objects;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.CanvasObject;
import net.stzups.board.data.objects.canvas.CanvasObjectType;

public class Point extends CanvasObject {
    public int dt;
    public short x;
    public short y;

    public Point(int dt, short x, short y) {
        super(CanvasObjectType.POINT);
        this.dt = dt;
        this.x = x;
        this.y = y;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
    }
}
