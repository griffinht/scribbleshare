package net.stzups.board.server.websocket.protocol.client;

/**
 * Represents a packet sent by the client
 */
public abstract class ClientMessage {
    private ClientMessageType packetType;

    protected ClientMessage(ClientMessageType packetType) {
        this.packetType = packetType;
    }

    public ClientMessageType getMessageType() {
        return packetType;
    }
}
