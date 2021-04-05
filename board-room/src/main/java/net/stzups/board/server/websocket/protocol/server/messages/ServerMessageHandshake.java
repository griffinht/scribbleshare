package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.PersistentUserSession;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageHandshake extends ServerMessage {
    private long id;
    private long token;

    public ServerMessageHandshake(PersistentUserSession persistentUserSession) {
        super(ServerMessageType.HANDSHAKE);
        this.id = persistentUserSession.getId();
        this.token = persistentUserSession.generateToken();
    }

    @Override
    public void serialize(ByteBuf bytebuf) {
        super.serialize(bytebuf);
        bytebuf.writeLong(id);
        bytebuf.writeLong(token);
    }
}
