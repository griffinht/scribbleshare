package net.stzups.scribbleshare.room.server.websocket;

import io.netty.buffer.ByteBuf;

public class MouseMove {
    private final byte dt;
    private final short x;
    private final short y;

    public MouseMove(ByteBuf byteBuf) {
        this.dt = byteBuf.readByte();
        this.x = byteBuf.readShort();
        this.y = byteBuf.readShort();
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(dt);
        byteBuf.writeShort(x);
        byteBuf.writeShort(y);
    }

    @Override
    public String toString() {
        return MouseMove.class.getSimpleName() + "{dt=" + dt + ",x=" + x + ",y=" + y + "}";
    }
}
