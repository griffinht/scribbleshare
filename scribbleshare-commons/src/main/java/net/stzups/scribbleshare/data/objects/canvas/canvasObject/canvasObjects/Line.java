package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;

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
    }

    private final List<Point> points = new ArrayList<>();
    private final byte red;
    private final byte green;
    private final byte blue;

    public Line(ByteBuf byteBuf) {
        super(byteBuf);
        this.red = byteBuf.readByte();
        this.green = byteBuf.readByte();
        this.blue = byteBuf.readByte();
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
        byteBuf.writeByte(red);
        byteBuf.writeByte(green);
        byteBuf.writeByte(blue);
        byteBuf.writeByte((byte) points.size());
        for (Point point : points) {
            point.serialize(byteBuf);
        }
    }
}
