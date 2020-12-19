package net.stzups.board.room.protocol.packets;

import net.stzups.board.room.protocol.PacketType;

public abstract class Packet {
    private PacketType packetType;

    Packet(PacketType packetType) {
        this.packetType = packetType;
    }

    public PacketType getPacketType() {
        return packetType;
    }
}
