package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.UserSession;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageHandshake extends ServerMessage {
    private long token;
    private long userId;

    public ServerMessageHandshake(UserSession userSession) {
        super(ServerMessageType.HANDSHAKE);
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
