package net.stzups.board.protocol.server;

import net.stzups.board.room.Document;

public class ServerPacketOpen extends ServerPacket {
    private Document document;

    public ServerPacketOpen(Document document) {
        super(ServerPacketType.OPEN);
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}
