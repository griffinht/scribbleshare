package net.stzups.board.server.websocket.protocol.client.messages;

import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageHandshake extends ClientMessage {
    private long token;

    public ClientMessageHandshake(long token) {
        super(ClientMessageType.HANDSHAKE);
        this.token = token;
    }

    public long getToken() {
        return token;
    }
}
