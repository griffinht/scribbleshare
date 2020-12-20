package net.stzups.board.room.protocol.server;

public abstract class ServerPacketId extends ServerPacket {
    private int id;

    ServerPacketId(ServerPacketType packetType, int id) {
        super(packetType);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
