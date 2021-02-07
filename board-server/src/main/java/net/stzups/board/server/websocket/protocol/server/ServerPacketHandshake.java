package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;

public class ServerPacketHandshake extends ServerPacket {
    private long token;

    public ServerPacketHandshake(long token) {
        super(ServerPacketType.HANDSHAKE);
        this.token = token;
    }

    @Override
    public void serialize(ByteBuf bytebuf) {
        super.serialize(bytebuf);
        System.out.println(token + "posdgsdg");
        bytebuf.writeLong(token);
    }
}
