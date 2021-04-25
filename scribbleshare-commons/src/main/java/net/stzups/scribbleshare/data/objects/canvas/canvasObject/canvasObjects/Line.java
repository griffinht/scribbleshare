package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;

import java.util.ArrayList;
import java.util.List;

public class Line extends CanvasObject {
    public static class Point {
        private final short x;
        private final short y;

        private Point(ByteBuf byteBuf) {
            this.x = byteBuf.readShort();
            this.y = byteBuf.readShort();
        }

        public Point(short x, short y) {
            this.x = x;
            this.y = y;
        }

        private void serialize(ByteBuf byteBuf) {
            byteBuf.writeShort(x);
            byteBuf.writeShort(y);
        }
    }

    private final List<Point> points = new ArrayList<>();

    public Line(ByteBuf byteBuf) {
        super(byteBuf);
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++)  {
            points.add(new Point(byteBuf));
        }
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) points.size());
        for (Point point : points) {
            point.serialize(byteBuf);
        }
    }
}
