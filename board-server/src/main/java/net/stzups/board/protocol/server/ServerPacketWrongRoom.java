package net.stzups.board.protocol.server;

public class ServerPacketWrongRoom extends ServerPacket {

    public ServerPacketWrongRoom() {
        super(ServerPacketType.WRONG_ROOM);
    }
}
