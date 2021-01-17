package net.stzups.board.protocol.server;

import net.stzups.board.room.Document;

public class ServerPacketOpenDocument extends ServerPacket {
    private Document document;

    public ServerPacketOpenDocument(Document document) {
        super(ServerPacketType.OPEN_DOCUMENT);
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}
