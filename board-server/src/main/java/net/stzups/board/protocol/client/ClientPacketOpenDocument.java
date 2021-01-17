package net.stzups.board.protocol.client;

public class ClientPacketOpenDocument extends ClientPacket {
    private String id;

    public ClientPacketOpenDocument(String id) {
        super(ClientPacketType.OPEN_DOCUMENT);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
