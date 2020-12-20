package net.stzups.board.room.protocol.client;

public class ClientPacketOffsetDraw extends ClientPacket {
    private int x;
    private int y;

    public ClientPacketOffsetDraw(int x, int y) {
        super(ClientPacketType.OFFSET_DRAW);
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
