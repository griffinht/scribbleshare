package net.stzups.board.server.websocket.protocol.client;

import net.stzups.board.data.objects.Point;

public class ClientPacketDraw extends ClientPacket {
    private Point[] points;

    public ClientPacketDraw(Point[] points) {
        super(ClientPacketType.DRAW);
        this.points = points;
    }

    public Point[] getPoints() {
        return points;
    }
}
