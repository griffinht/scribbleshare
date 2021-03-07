package net.stzups.board.server.websocket.protocol.client;

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
