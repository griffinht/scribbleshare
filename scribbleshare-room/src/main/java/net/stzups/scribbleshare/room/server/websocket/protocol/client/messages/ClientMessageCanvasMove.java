package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.CanvasMove;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

import java.util.HashMap;
import java.util.Map;

public class ClientMessageCanvasMove extends ClientMessage {

    private final Map<Short, CanvasMove[]> canvasMovesMap = new HashMap<>();

    public ClientMessageCanvasMove(ByteBuf byteBuf) {
        super(ClientMessageType.CANVAS_MOVE);
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            short id = byteBuf.readShort();
            CanvasMove[] canvasObjectWrappers = new CanvasMove[byteBuf.readUnsignedByte()];
            for (int j = 0; j < canvasObjectWrappers.length; j++) {
                canvasObjectWrappers[j] = new CanvasMove(byteBuf);
            }
            canvasMovesMap.put(id, canvasObjectWrappers);
        }
    }

    public Map<Short, CanvasMove[]> getCanvasMovesMap() {
        return canvasMovesMap;
    }
}
