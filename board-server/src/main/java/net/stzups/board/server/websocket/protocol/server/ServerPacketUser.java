package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.User;

public abstract class ServerPacketUser extends ServerPacket {
    private User user;

    ServerPacketUser(ServerPacketType serverPacketType, User user) {
        super(serverPacketType);
        this.user = user;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(user.getId());
    }
}
