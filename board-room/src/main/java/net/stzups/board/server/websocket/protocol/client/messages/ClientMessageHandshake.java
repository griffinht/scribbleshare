package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageHandshake extends ClientMessage {
    private long id;
    private long token;

    public ClientMessageHandshake(ByteBuf byteBuf) {
        super(ClientMessageType.HANDSHAKE);
        this.id = byteBuf.readLong();
        this.token = byteBuf.readLong();
    }

    public long getId() {
        return id;
    }

    public long getToken() {
        return token;
    }
}
