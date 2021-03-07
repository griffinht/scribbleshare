package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.Client;

public class ServerPacketAddClient extends ServerPacketClient {
    private Client client;

    public ServerPacketAddClient(Client client) {
        super(ServerPacketType.ADD_CLIENT, client);
        this.client = client;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(client.getUser().getId());
    }
}
