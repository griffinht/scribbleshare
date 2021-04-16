package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageDeleteDocument extends ClientMessage {
    private final long id;

    public ClientMessageDeleteDocument(ByteBuf byteBuf) {
        super(ClientMessageType.DELETE_DOCUMENT);
        this.id = byteBuf.readLong();
    }

    public long id() {
        return id;
    }
}
