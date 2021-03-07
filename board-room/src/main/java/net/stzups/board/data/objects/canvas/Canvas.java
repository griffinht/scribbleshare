package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Canvas extends ServerMessage {
    private Map<CanvasObjectType, List<CanvasObject>> canvasObjectsMap = new HashMap<>();
    private Map<CanvasObjectType, List<CanvasObject>> updatedCanvasObjectMap = new HashMap<>();

    protected Canvas() {
        super(ServerMessageType.DRAW);
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort((byte) canvasObjectsMap.size());
        for (Map.Entry<CanvasObjectType, List<CanvasObject>> canvasObjects : canvasObjectsMap.entrySet()) {
            byteBuf.writeByte((byte) canvasObjects.getKey().getId());
            byteBuf.writeShort(canvasObjects.getValue().size());
            for (CanvasObject canvasObject : canvasObjects.getValue()) {
                canvasObject.serialize(byteBuf);
            }
        }
    }
}
