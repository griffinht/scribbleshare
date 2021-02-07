package net.stzups.board.server.websocket.protocol.client;

public class ClientPacketOpenDocument extends ClientPacket {
    private long id;

    public ClientPacketOpenDocument(long id) {
        super(ClientPacketType.OPEN_DOCUMENT);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
