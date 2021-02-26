package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Point;
import net.stzups.board.data.objects.User;
import net.stzups.board.server.websocket.Client;

public class ServerPacketDrawClient extends ServerPacketClient {
    private Point[] points;

    public ServerPacketDrawClient(Client client, Point[] points) {
        super(ServerPacketType.DRAW, client);
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
