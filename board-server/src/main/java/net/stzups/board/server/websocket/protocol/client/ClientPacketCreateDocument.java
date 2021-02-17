package net.stzups.board.server.websocket.protocol.client;

public class ClientPacketCreateDocument extends ClientPacket {
    public ClientPacketCreateDocument() {
        super(ClientPacketType.CREATE_DOCUMENT);
    }
}
