package net.stzups.board.protocol.client;

public class ClientPacketCreateDocument extends ClientPacket {
    public ClientPacketCreateDocument() {
        super(ClientPacketType.CREATE_DOCUMENT);
    }
}
