package net.stzups.board.server.websocket.protocol.client;

public class ClientPacketHandshake extends ClientPacket {
    public ClientPacketHandshake() {
        super(ClientPacketType.HANDSHAKE);
    }
}
