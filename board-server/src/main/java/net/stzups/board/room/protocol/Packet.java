package net.stzups.board.room.protocol;

import net.stzups.board.room.protocol.client.ClientPacketType;

public abstract class Packet {
    private ClientPacketType packetType;

    Packet(ClientPacketType packetType) {
        this.packetType = packetType;
    }

    public ClientPacketType getPacketType() {
        return packetType;
    }
}
