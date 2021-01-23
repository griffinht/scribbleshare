package net.stzups.board.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Document;

public class ServerPacketAddDocument extends ServerPacket {
    private Document document;

    public ServerPacketAddDocument(Document document) {
        super(ServerPacketType.ADD_DOCUMENT);
        this.document = document;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        writeString(document.getId(), byteBuf);
        writeString(document.getName(), byteBuf);
    }
}
