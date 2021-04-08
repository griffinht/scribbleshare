package net.stzups.board.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Document;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageUpdateDocument extends ServerMessage {
    private Document document;
    private boolean shared;

    public ServerMessageUpdateDocument(Document document) {
        this(document, false);
    }
    public ServerMessageUpdateDocument(Document document, boolean shared) {
        super(ServerMessageType.UPDATE_DOCUMENT);
        this.document = document;
        this.shared = shared;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeBoolean(shared);//takes up one byte
        byteBuf.writeLong(document.getId());
        writeString(document.getName(), byteBuf);
    }
}
