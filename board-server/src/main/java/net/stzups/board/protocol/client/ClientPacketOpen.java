package net.stzups.board.protocol.client;

public class ClientPacketOpen extends ClientPacket {
    public ClientPacketOpen() {
        super(ClientPacketType.OPEN);
    }
}
