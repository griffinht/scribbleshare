package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.Client;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;
import net.stzups.util.DebugString;

public class ServerMessageRemoveClient extends ServerMessage {
    private final Client client;

    public ServerMessageRemoveClient(Client client) {
        this.client = client;
    }

    @Override
    protected ServerMessageType getMessageType() {
        return ServerMessageType.REMOVE_CLIENT;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(client.getId());
    }

    @Override
    public String toString() {
        return DebugString.get(ServerMessageRemoveClient.class)
                .add("client", client)
                .toString();
    }
}
