package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageDeleteDocument extends ClientMessage {
    private final long id;

    public ClientMessageDeleteDocument(ByteBuf byteBuf) {
        this.id = byteBuf.readLong();
    }

    @Override
    public ClientMessageType getMessageType() {
        return ClientMessageType.DELETE_DOCUMENT;
    }

    public long id() {
        return id;
    }
}
