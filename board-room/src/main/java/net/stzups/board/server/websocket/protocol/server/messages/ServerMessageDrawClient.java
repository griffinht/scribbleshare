package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.Point;
import net.stzups.board.server.websocket.Client;
import net.stzups.board.server.websocket.protocol.server.ServerMessageClient;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageDrawClient extends ServerMessageClient {
    private Point[] points;

    public ServerMessageDrawClient(Client client, Point[] points) {
        super(ServerMessageType.DRAW, client);
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
