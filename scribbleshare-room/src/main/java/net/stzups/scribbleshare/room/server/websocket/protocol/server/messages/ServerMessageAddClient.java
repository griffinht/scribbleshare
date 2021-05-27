package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.Client;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;
import net.stzups.scribbleshare.util.DebugString;

import java.util.Collections;
import java.util.Set;

public class ServerMessageAddClient extends ServerMessage {
    private final Set<Client> clients;

    public ServerMessageAddClient(Client client) {
        this(Collections.singleton(client));
    }

    public ServerMessageAddClient(Set<Client> clients) {
        this.clients = clients;
    }

    @Override
    protected ServerMessageType getMessageType() {
        return ServerMessageType.ADD_CLIENT;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort((short) clients.size());
        for (Client client : clients) {
            client.serialize(byteBuf);
        }
    }

    @Override
    public String toString() {
        return DebugString.get(ServerMessageAddClient.class)
                .add("clients", clients)
                .toString();
    }
}
