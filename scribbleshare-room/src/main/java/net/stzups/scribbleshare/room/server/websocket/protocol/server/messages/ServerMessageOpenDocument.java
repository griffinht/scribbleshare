package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageOpenDocument extends ServerMessage {
    private final Document document;
    private final Canvas canvas;

    public ServerMessageOpenDocument(Document document, Canvas canvas) {
        this.document = document;
        this.canvas = canvas;
    }

    @Override
    protected ServerMessageType getMessageType() {
        return ServerMessageType.OPEN_DOCUMENT;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(document.getId());
        canvas.serialize(byteBuf);
    }
}
