package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.Client;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageRemoveClient extends ServerMessage {
    Client client;

    public ServerMessageRemoveClient(Client client) {
        super(ServerMessageType.REMOVE_CLIENT);
        this.client = client;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(client.getId());
    }
}
