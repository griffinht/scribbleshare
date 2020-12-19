package net.stzups.board.room.protocol.packets;

import net.stzups.board.room.protocol.PacketType;

public class PacketOffsetDraw extends Packet {
    private int x;
    private int y;

    public PacketOffsetDraw(int x, int y) {
        super(PacketType.OFFSET_DRAW);
        this.x = x;
        this.y = y;
    }

    public int getOffsetX() {
        return x;
    }

    public int getOffsetY() {
        return y;
    }
}
