package net.stzups.board.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.Client;

public class ServerMessageAddClient extends ServerMessageClient {
    private Client client;

    public ServerMessageAddClient(Client client) {
        super(ServerMessageType.ADD_CLIENT, client);
        this.client = client;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(client.getUser().getId());
    }
}
