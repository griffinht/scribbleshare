package net.stzups.board.server.websocket.protocol.server;

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
        byteBuf.writeLong(document.getId());
        writeString(document.getName(), byteBuf);
    }
}
