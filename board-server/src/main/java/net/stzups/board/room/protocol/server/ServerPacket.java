package net.stzups.board.room.protocol.server;

/**
 * Represents a packet sent by the server
 */
public abstract class ServerPacket {
    private ServerPacketType packetType;

    ServerPacket(ServerPacketType packetType) {
        this.packetType = packetType;
    }

    public ServerPacketType getPacketType() {
        return packetType;
    }
}
