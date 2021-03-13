package net.stzups.board.server.websocket.protocol.client.messages;

import net.stzups.board.data.objects.canvas.Point;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

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
