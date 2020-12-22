package net.stzups.board.protocol.server;

public class ServerPacketOpen extends ServerPacket {
    private String id;

    public ServerPacketOpen(String id) {
        super(ServerPacketType.OPEN);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
