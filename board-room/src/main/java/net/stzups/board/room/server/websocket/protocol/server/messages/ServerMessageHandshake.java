package net.stzups.board.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.session.PersistentHttpSession;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageHandshake extends ServerMessage {
    private long id;
    private long token;

    public ServerMessageHandshake(PersistentHttpSession persistentHttpSession) {
        super(ServerMessageType.HANDSHAKE);
        this.id = persistentHttpSession.getId();
        //this.token = persistentHttpSession.generateToken();
    }

    @Override
    public void serialize(ByteBuf bytebuf) {
        super.serialize(bytebuf);
        bytebuf.writeLong(id);
        bytebuf.writeLong(token);
    }
}
