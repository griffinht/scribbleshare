package net.stzups.board.room.protocol.client;

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
