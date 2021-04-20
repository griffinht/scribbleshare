package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.CanvasInsert;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

import java.util.Map;

public class ServerMessageCanvasInsert extends ServerMessage {
    Map<CanvasObjectType, CanvasInsert[]> canvasInsertsMap;

    public ServerMessageCanvasInsert(Map<CanvasObjectType, CanvasInsert[]> canvasInsertsMap) {
        super(ServerMessageType.CANVAS_INSERT);
        this.canvasInsertsMap = canvasInsertsMap;
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) canvasInsertsMap.size());
        for (Map.Entry<CanvasObjectType, CanvasInsert[]> entry : canvasInsertsMap.entrySet()) {
            byteBuf.writeByte((byte) entry.getKey().getId());
            byteBuf.writeByte((byte) entry.getValue().length);
            for (CanvasInsert canvasInsert : entry.getValue()) {
                canvasInsert.serialize(byteBuf);
            }
        }
    }
}
