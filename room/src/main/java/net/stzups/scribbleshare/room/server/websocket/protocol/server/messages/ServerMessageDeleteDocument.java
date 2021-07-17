package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;
import net.stzups.util.DebugString;

public class ServerMessageDeleteDocument extends ServerMessage {
    private final long id;

    public ServerMessageDeleteDocument(Document document) {
        id = document.getId();
    }

    @Override
    protected ServerMessageType getMessageType() {
        return ServerMessageType.DELETE_DOCUMENT;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(id);
    }

    @Override
    public String toString() {
        return DebugString.get(ServerMessageDeleteDocument.class)
                .add("id", id)
                .toString();
    }
}
