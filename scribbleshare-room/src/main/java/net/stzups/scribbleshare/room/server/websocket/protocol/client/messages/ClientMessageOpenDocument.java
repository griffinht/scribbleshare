package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;
import net.stzups.scribbleshare.util.DebugString;

public class ClientMessageOpenDocument extends ClientMessage {
    private final long id;

    public ClientMessageOpenDocument(ByteBuf byteBuf) {
        this.id = byteBuf.readLong();
    }

    @Override
    public ClientMessageType getMessageType() {
        return ClientMessageType.OPEN_DOCUMENT;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return DebugString.get(ClientMessageOpenDocument.class)
                .add("id", id)
                .toString();
    }
}
