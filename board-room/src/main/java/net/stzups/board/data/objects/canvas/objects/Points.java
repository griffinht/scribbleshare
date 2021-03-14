package net.stzups.board.data.objects.canvas.objects;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.User;
import net.stzups.board.data.objects.canvas.CanvasObject;
import net.stzups.board.data.objects.canvas.CanvasObjectType;

public class Points extends CanvasObject {
    static class Point {
        public int dt;//todo byte type instead of int?
        public short x;
        public short y;

        public Point(int dt, short x, short y) {
            this.dt = dt;
            this.x = x;
            this.y = y;
        }
    }

    private Point[] points;

    public Points(ByteBuf byteBuf) {
        super(CanvasObjectType.POINTS, byteBuf);
        points = new Point[byteBuf.readShort()];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(byteBuf.readByte(), byteBuf.readShort(), byteBuf.readShort());
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(points.length);
        for (Point point : points) {
            byteBuf.writeByte((byte) point.dt);
            byteBuf.writeShort(point.x);
            byteBuf.writeShort(point.y);
        }
    }
}
