package net.stzups.board.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.room.server.websocket.Client;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessageType;

import java.util.Collections;
import java.util.Set;

public class ServerMessageAddClient extends ServerMessage {
    private Set<Client> clients;

    public ServerMessageAddClient(Client client) {
        this(Collections.singleton(client));
    }

    public ServerMessageAddClient(Set<Client> clients) {
        super(ServerMessageType.ADD_CLIENT);
        this.clients = clients;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort((short) clients.size());
        for (Client client : clients) {
            client.serialize(byteBuf);
        }
    }
}
