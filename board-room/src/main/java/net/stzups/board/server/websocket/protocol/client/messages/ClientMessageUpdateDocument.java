package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.Canvas;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageUpdateDocument extends ClientMessage {
    private Canvas canvas;

    public ClientMessageUpdateDocument(ByteBuf byteBuf) {
        super(ClientMessageType.UPDATE_DOCUMENT);
        this.canvas = new Canvas(byteBuf);
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
