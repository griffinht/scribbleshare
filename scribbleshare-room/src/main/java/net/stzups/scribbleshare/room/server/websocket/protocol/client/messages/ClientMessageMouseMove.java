package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.MouseMove;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageMouseMove extends ClientMessage {
    private final MouseMove[] mouseMoves;

    public ClientMessageMouseMove(ByteBuf byteBuf) {
        super(ClientMessageType.MOUSE_MOVE);
        mouseMoves = new MouseMove[byteBuf.readUnsignedByte()];
        for (int i = 0; i < mouseMoves.length; i++) {
            mouseMoves[i] = new MouseMove(byteBuf);
        }
    }

    public MouseMove[] getMouseMoves() {
        return mouseMoves;
    }
}
