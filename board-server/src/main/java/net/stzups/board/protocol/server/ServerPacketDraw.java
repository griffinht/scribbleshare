package net.stzups.board.protocol.server;

import net.stzups.board.protocol.Point;

public class ServerPacketDraw extends ServerPacketId implements ServerPacketInterval {
    private Point[] points;

    public ServerPacketDraw(int id, Point[] points) {
        super(ServerPacketType.DRAW, id);
        this.points = points;
    }

    public Point[] getPoints() {
        return points;
    }
}
