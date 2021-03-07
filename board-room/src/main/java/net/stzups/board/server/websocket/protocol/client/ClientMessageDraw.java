package net.stzups.board.server.websocket.protocol.client;

import net.stzups.board.data.objects.canvas.Point;

public class ClientMessageDraw extends ClientMessage {
    private Point[] points;

    public ClientMessageDraw(Point[] points) {
        super(ClientMessageType.DRAW);
        this.points = points;
    }

    public Point[] getPoints() {
        return points;
    }
}
