package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;
import net.stzups.util.D.DebugString;

public class ServerMessageUpdateDocument extends ServerMessage {
    private final Document document;
    private final boolean shared;

    public ServerMessageUpdateDocument(Document document) {
        this(document, false);
    }
    public ServerMessageUpdateDocument(Document document, boolean shared) {
        this.document = document;
        this.shared = shared;
    }

    @Override
    protected ServerMessageType getMessageType() {
        return ServerMessageType.UPDATE_DOCUMENT;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeBoolean(shared);//takes up one byte
        byteBuf.writeLong(document.getId());
        writeString(document.getName(), byteBuf);
    }

    @Override
    public String toString() {
        return DebugString.get(ServerMessageUpdateDocument.class)
                .add("document", document)
                .add("shared", shared)
                .toString();
    }
}
