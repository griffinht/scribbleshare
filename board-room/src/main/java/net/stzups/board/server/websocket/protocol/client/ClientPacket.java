package net.stzups.board.server.websocket.protocol.client;

/**
 * Represents a packet sent by the client
 */
public abstract class ClientPacket {
    private ClientPacketType packetType;

    ClientPacket(ClientPacketType packetType) {
        this.packetType = packetType;
    }

    public ClientPacketType getPacketType() {
        return packetType;
    }
}
