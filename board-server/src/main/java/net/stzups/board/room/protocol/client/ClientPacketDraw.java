package net.stzups.board.room.protocol.client;

import net.stzups.board.room.protocol.server.ServerPacketId;
import net.stzups.board.room.protocol.server.ServerPacketType;

public class ClientPacketDraw extends ClientPacket {
    private int x;
    private int y;

    public ClientPacketDraw(int x, int y) {
        super(ClientPacketType.DRAW);
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
