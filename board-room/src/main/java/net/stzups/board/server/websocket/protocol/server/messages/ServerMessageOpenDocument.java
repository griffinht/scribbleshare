package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Document;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageOpenDocument extends ServerMessage {
    private Document document;

    public ServerMessageOpenDocument(Document document) {
        super(ServerMessageType.OPEN_DOCUMENT);
        this.document = document;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(document.getId());
    }
}
