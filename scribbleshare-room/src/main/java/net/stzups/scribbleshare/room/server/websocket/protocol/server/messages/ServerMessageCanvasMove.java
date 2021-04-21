package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.CanvasMove;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

import java.util.Map;

public class ServerMessageCanvasMove extends ServerMessage {
    private final Map<Short, CanvasMove[]> canvasMovesMap;

    public ServerMessageCanvasMove(Map<Short, CanvasMove[]> canvasMovesMap) {
        super(ServerMessageType.CANVAS_MOVE);
        this.canvasMovesMap = canvasMovesMap;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) canvasMovesMap.size());
        for (Map.Entry<Short, CanvasMove[]> entry : canvasMovesMap.entrySet()) {
            byteBuf.writeShort(entry.getKey());
            byteBuf.writeByte((byte) entry.getValue().length);
            for (CanvasMove canvasMove : entry.getValue()) {
                canvasMove.serialize(byteBuf);
            }
        }
    }
}
