package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;
import net.stzups.util.D.DebugString;

public class ClientMessageUpdateDocument extends ClientMessage {
    private final long id;
    private final String name;

    public ClientMessageUpdateDocument(ByteBuf byteBuf) {
        id = byteBuf.readLong();
        name = readString(byteBuf);
    }

    @Override
    public ClientMessageType getMessageType() {
        return ClientMessageType.UPDATE_DOCUMENT;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return DebugString.get(ClientMessageUpdateDocument.class)
                .add("id", id)
                .add("name", name)
                .toString();
    }
}
