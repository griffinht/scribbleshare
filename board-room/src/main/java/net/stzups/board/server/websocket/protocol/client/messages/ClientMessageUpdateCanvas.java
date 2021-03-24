package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageUpdateCanvas extends ClientMessage {
    public ClientMessageUpdateCanvas(ByteBuf byteBuf) {
        super(ClientMessageType.UPDATE_CANVAS);
        //canvasState = new CanvasState(byteBuf);
    }
/*
    public CanvasState getCanvasState() {
        return canvasState;
    }*/
}
