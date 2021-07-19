package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Color;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.util.DebugString;

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

        private void serialize(ByteBuf byteBuf) {
            byteBuf.writeShort(x);
            byteBuf.writeShort(y);
        }

        @Override
        public String toString() {
            return DebugString.get(Point.class)
                    .add("x", x)
                    .add("y", y)
                    .toString();
        }
    }

    private final List<Point> points = new ArrayList<>();
    private final Color color;

    public Line(ByteBuf byteBuf) {
        super(byteBuf);
        color = new Color(byteBuf);
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++)  {
            points.add(new Point(byteBuf));
        }
    }

    @Override
    public CanvasObjectType getCanvasObjectType() {
        return CanvasObjectType.LINE;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        color.serialize(byteBuf);
        byteBuf.writeByte((byte) points.size());
        for (Point point : points) {
            point.serialize(byteBuf);
        }
    }

    @Override
    public String toString() {
        return DebugString.get(Line.class, super.toString())
                .add("points", points)
                .add("color", color)
                .toString();
    }
}
