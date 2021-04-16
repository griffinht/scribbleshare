package net.stzups.board.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Document;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageDeleteDocument extends ServerMessage {
    private final long id;

    public ServerMessageDeleteDocument(Document document) {
        super(ServerMessageType.DELETE_DOCUMENT);
        id = document.getId();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(id);
    }
}
