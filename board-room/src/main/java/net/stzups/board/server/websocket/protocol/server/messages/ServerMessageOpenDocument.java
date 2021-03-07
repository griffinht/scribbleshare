package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.objects.Point;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

import java.util.List;
import java.util.Map;

public class ServerMessageOpenDocument extends ServerMessage {
    private Document document;

    public ServerMessageOpenDocument(Document document) {
        super(ServerMessageType.OPEN_DOCUMENT);
        this.document = document;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(document.getId());
        //OPEN_DOCUMENT is serialized, now serialize the other things
        byteBuf.writeShort((short) document.getPoints().size());
        for (Map.Entry<User, List<Point>> entry : document.getPoints().entrySet()) {
            byteBuf.writeLong(entry.getKey().getId());
            byteBuf.writeShort((short) entry.getValue().size());
            for (Point point : entry.getValue()) {
                int dt = point.dt;
                if (dt != 0) {
                    dt = -1;
                }
                byteBuf.writeByte((byte) dt);
                byteBuf.writeShort(point.x);
                byteBuf.writeShort(point.y);
            }
        }
    }
}
