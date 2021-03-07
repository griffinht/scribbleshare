package net.stzups.board.server.websocket.protocol.client;

public class ClientMessageOpenDocument extends ClientMessage {
    private long id;

    public ClientMessageOpenDocument(long id) {
        super(ClientMessageType.OPEN_DOCUMENT);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
