package net.stzups.board.room.protocol.server;

public class ServerPacketDraw extends ServerPacketId {
    private int x;
    private int y;

    public ServerPacketDraw(int id, int x, int y) {
        super(ServerPacketType.DRAW, id);
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
