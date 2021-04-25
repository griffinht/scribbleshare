package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;

public class Line extends CanvasObject {
    private static class Point {
        private final short x;
        private final short y;

        private Point(ByteBuf byteBuf) {
            this.x = byteBuf.readShort();
            this.y = byteBuf.readShort();
        }

        private void serialize(ByteBuf byteBuf) {
            byteBuf.writeShort(x);
            byteBuf.writeShort(y);
        }
    }

    private final Point[] points;

    public Line(ByteBuf byteBuf) {
        super(byteBuf);
        points = new Point[byteBuf.readUnsignedByte()];
        for (int i = 0; i < points.length; i++)  {
            points[i] = new Point(byteBuf);
        }
    }

    public Point[] getPoints() {
        return points;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) points.length);
        for (Point point : points) {
            point.serialize(byteBuf);
        }
    }
}
