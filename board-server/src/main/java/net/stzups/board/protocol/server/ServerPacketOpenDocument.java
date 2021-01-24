package net.stzups.board.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Point;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.User;

import java.util.List;
import java.util.Map;

public class ServerPacketOpenDocument extends ServerPacket {
    private Document document;

    public ServerPacketOpenDocument(Document document) {
        super(ServerPacketType.OPEN_DOCUMENT);
        this.document = document;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        writeString(document.getId(), byteBuf);
        //OPEN_DOCUMENT is serialized, now serialize the other things
        Map<User, List<Point>> pointsMap = document.getPoints();
        for (Map.Entry<User, List<Point>> entry : pointsMap.entrySet()) {
            new ServerPacketAddUser(entry.getKey()).serialize(byteBuf);//todo this might be bad design/break some things
            Point[] points = new Point[entry.getValue().size()];
            int i = 0;
            for (Point point : entry.getValue()) {
                if (point.dt != 0) {
                    point.dt = -1;
                }
                points[i++] = point;
            }
            new ServerPacketDraw(entry.getKey(), points).serialize(byteBuf);
        }
    }
}
