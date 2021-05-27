package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;
import net.stzups.scribbleshare.util.DebugString;

public class ClientMessageDeleteDocument extends ClientMessage {
    private final long id;

    public ClientMessageDeleteDocument(ByteBuf byteBuf) {
        this.id = byteBuf.readLong();
    }

    @Override
    public ClientMessageType getMessageType() {
        return ClientMessageType.DELETE_DOCUMENT;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return DebugString.get(ClientMessageDeleteDocument.class)
                .add("id", id)
                .toString();
    }
}
