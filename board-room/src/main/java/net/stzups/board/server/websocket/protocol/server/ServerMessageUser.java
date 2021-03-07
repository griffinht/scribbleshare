package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.User;

public abstract class ServerMessageUser extends ServerMessage {
    private User user;

    ServerMessageUser(ServerMessageType serverMessageType, User user) {
        super(serverMessageType);
        this.user = user;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(user.getId());
    }
}
