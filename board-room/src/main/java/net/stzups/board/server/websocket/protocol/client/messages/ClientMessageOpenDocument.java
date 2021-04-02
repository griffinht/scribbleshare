package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageOpenDocument extends ClientMessage {
    private long id;

    public ClientMessageOpenDocument(ByteBuf byteBuf) {
        super(ClientMessageType.OPEN_DOCUMENT);
        this.id = byteBuf.readLong();
    }

    public long getId() {
        return id;
    }
}
