package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.Client;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageHandshake extends ServerMessage {
    private final Client client;

    public ServerMessageHandshake(Client client) {
        this.client = client;
    }

    @Override
    protected ServerMessageType getMessageType() {
        return ServerMessageType.HANDSHAKE;
    }

    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(client.getId());
    }
}
