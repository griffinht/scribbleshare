package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.Canvas;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageCanvas extends ClientMessage {
    private Canvas canvas;

    public ClientMessageCanvas(ByteBuf byteBuf) {
        super(ClientMessageType.CANVAS);
        this.canvas = new Canvas(byteBuf);
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
