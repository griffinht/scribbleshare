package net.stzups.board.protocol.client;

public class ClientPacketCreateDocument extends ClientPacket {
    private String name;

    public ClientPacketCreateDocument(String name) {
        super(ClientPacketType.CREATE_DOCUMENT);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
