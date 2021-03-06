package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.UserSession;

public class ServerPacketHandshake extends ServerPacket {
    private long token;
    private long userId;

    public ServerPacketHandshake(UserSession userSession) {
        super(ServerPacketType.HANDSHAKE);
        this.token = userSession.getToken();
        this.userId = userSession.getUserId();
    }

    @Override
    public void serialize(ByteBuf bytebuf) {
        super.serialize(bytebuf);
        bytebuf.writeLong(token);
        bytebuf.writeLong(userId);
    }
}
