package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.Client;

public abstract class ServerMessageClient extends ServerMessage {
    private short id;

    protected ServerMessageClient(ServerMessageType packetType, Client client) {
        super(packetType);
        this.id = client == null ? 0 : client.getId();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(id);
    }
}
