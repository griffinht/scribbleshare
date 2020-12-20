package net.stzups.board.room.protocol.client;

public class ClientPacketOpen extends ClientPacket {
    public ClientPacketOpen() {
        super(ClientPacketType.OPEN);
    }
}
