package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.util.DebugString;

public class Color {
    private final byte red;
    private final byte green;
    private final byte blue;

    public Color(ByteBuf byteBuf) {
        red = byteBuf.readByte();
        green = byteBuf.readByte();
        blue = byteBuf.readByte();
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(red);
        byteBuf.writeByte(green);
        byteBuf.writeByte(blue);
    }

    @Override
    public String toString() {
        return DebugString.get(Color.class)
                .add("red", red & 0xff)
                .add("green", green & 0xff)
                .add("blue", blue & 0xff)
                .toString();
    }
}
