package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.Canvas;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageCanvas extends ServerMessage {
    private Canvas canvas;

    protected ServerMessageCanvas(Canvas canvas) {
        super(ServerMessageType.CANVAS);
        this.canvas = canvas;
    }

    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        canvas.serialize(byteBuf);
    }
}
