package net.stzups.board.protocol.client;

public class ClientPacketOpen extends ClientPacket {
    private String id;
    public ClientPacketOpen(String id) {
        super(ClientPacketType.OPEN);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
