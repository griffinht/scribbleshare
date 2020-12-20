package net.stzups.board.room.protocol.server;

import net.stzups.board.room.protocol.Packet;
import net.stzups.board.room.protocol.client.ClientPacketType;

public class ServerPacketOffsetDraw extends ServerPacketId {
    private int x;
    private int y;

    public ServerPacketOffsetDraw(int id, int x, int y) {
        super(ServerPacketType.OFFSET_DRAW, id);
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
