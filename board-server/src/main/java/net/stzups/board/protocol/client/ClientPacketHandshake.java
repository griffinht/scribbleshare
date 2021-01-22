package net.stzups.board.protocol.client;

public class ClientPacketHandshake extends ClientPacket {
    public ClientPacketHandshake() {
        super(ClientPacketType.HANDSHAKE);
    }
}
