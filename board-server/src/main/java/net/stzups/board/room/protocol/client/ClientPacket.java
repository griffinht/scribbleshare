package net.stzups.board.room.protocol.client;

public abstract class ClientPacket {
    private ClientPacketType packetType;

    ClientPacket(ClientPacketType packetType) {
        this.packetType = packetType;
    }

    public ClientPacketType getPacketType() {
        return packetType;
    }
}
