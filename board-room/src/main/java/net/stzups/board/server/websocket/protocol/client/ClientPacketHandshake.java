package net.stzups.board.server.websocket.protocol.client;

public class ClientPacketHandshake extends ClientPacket {
    private long token;

    public ClientPacketHandshake(long token) {
        super(ClientPacketType.HANDSHAKE);
        this.token = token;
    }

    public long getToken() {
        return token;
    }
}
