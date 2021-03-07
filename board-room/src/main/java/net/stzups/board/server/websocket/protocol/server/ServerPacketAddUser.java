package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.User;

public class ServerPacketAddUser extends ServerPacketUser {
    public ServerPacketAddUser(User user) {
        super(ServerPacketType.ADD_USER, user);
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
    }
}
