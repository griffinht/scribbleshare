package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageUpdateDocument extends ClientMessage {
    private long id;
    private String name;

    public ClientMessageUpdateDocument(ByteBuf byteBuf) {
        super(ClientMessageType.UPDATE_DOCUMENT);
        id = byteBuf.readLong();
        name = readString(byteBuf);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
