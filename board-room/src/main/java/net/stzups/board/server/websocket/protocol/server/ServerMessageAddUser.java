package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.User;

public class ServerMessageAddUser extends ServerMessageUser {
    public ServerMessageAddUser(User user) {
        super(ServerMessageType.ADD_USER, user);
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
    }
}
