package net.stzups.board.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.room.User;

public abstract class ServerPacketUser extends ServerPacket {
    private long id;

    ServerPacketUser(ServerPacketType packetType, User user) {
        super(packetType);
        this.id = user == null ? 0 : user.getId();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(id);
    }
}
