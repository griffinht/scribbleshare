package net.stzups.scribbleshare.data.objects.canvas.canvasObject;

import io.netty.buffer.ByteBuf;

public class LineCanvasObject extends CanvasObject {
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

    public LineCanvasObject(ByteBuf byteBuf) {
        super(byteBuf);
        points = new Point[byteBuf.readUnsignedByte()];
        for (int i = 0; i < points.length; i++)  {
            points[i] = new Point(byteBuf);
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) points.length);
        for (Point point : points) {
            point.serialize(byteBuf);
        }
    }
}
