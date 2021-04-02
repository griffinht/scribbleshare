package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.Document;
import net.stzups.board.data.objects.canvas.Canvas;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageOpenDocument extends ServerMessage {
    private Canvas canvas;

    public ServerMessageOpenDocument(Canvas canvas) {
        super(ServerMessageType.OPEN_DOCUMENT);
        this.canvas = canvas;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(this.canvas.getDocument().getId());
        this.canvas.serialize(byteBuf);
    }
}
