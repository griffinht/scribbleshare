package net.stzups.board.room.protocol.packets;

import net.stzups.board.room.protocol.PacketType;

public class PacketDraw extends Packet {
    private int x;
    private int y;

    public PacketDraw(int x, int y) {
        super(PacketType.DRAW);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
