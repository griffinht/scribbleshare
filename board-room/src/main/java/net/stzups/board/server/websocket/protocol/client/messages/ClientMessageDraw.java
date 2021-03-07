package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.objects.Point;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageDraw extends ClientMessage {
    private Point[] points;

    public ClientMessageDraw(ByteBuf byteBuf) {
        super(ClientMessageType.DRAW);
        Point[] points = new Point[byteBuf.readUnsignedByte()];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(byteBuf.readUnsignedByte(), byteBuf.readShort(), byteBuf.readShort());
        }
        this.points = points;
    }

    public Point[] getPoints() {
        return points;
    }
}
