package net.stzups.board.protocol.client;

public class ClientPacketOpen extends ClientPacket {
    private String id;
    private String name;

    public ClientPacketOpen(String id, String name) {
        super(ClientPacketType.OPEN);
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
