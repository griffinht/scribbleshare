package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageUpdateDocument extends ClientMessage {
    public ClientMessageUpdateDocument(ByteBuf byteBuf) {
        super(ClientMessageType.UPDATE_DOCUMENT);
        //canvasState = new CanvasState(byteBuf);
    }
/*
    public CanvasState getCanvasState() {
        return canvasState;
    }*/
}
