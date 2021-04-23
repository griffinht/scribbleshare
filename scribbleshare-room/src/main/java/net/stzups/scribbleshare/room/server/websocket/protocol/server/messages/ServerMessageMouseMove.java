package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.Client;
import net.stzups.scribbleshare.room.server.websocket.MouseMove;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageMouseMove extends ServerMessage {
    private final Client client;
    private final MouseMove[] mouseMoves;

    public ServerMessageMouseMove(Client client, MouseMove[] mouseMoves) {
        super(ServerMessageType.MOUSE_MOVE);
        this.client = client;
        this.mouseMoves = mouseMoves;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        client.serialize(byteBuf);
        byteBuf.writeByte((byte) mouseMoves.length);
        for (MouseMove mouseMove : mouseMoves) {
            mouseMove.serialize(byteBuf);
        }
    }
}
