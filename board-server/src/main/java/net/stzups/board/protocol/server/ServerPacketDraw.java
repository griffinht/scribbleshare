package net.stzups.board.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.protocol.Point;
import net.stzups.board.room.User;

public class ServerPacketDraw extends ServerPacketUser {
    private Point[] points;

    public ServerPacketDraw(User user, Point[] points) {
        super(ServerPacketType.DRAW, user);
        this.points = points;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort((short) points.length);
        for (Point point : points) {
            byteBuf.writeByte((byte) point.dt);
            byteBuf.writeShort(point.x);
            byteBuf.writeShort(point.y);
        }
    }
}
