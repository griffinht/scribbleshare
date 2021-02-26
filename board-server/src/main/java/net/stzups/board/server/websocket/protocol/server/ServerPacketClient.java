package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.User;
import net.stzups.board.server.websocket.Client;

public abstract class ServerPacketClient extends ServerPacket {
    private short id;

    ServerPacketClient(ServerPacketType packetType, Client client) {
        super(packetType);
        this.id = client == null ? 0 : client.getId();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(id);
    }
}
