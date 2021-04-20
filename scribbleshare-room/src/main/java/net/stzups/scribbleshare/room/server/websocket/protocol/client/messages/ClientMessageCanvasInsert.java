package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.CanvasInsert;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

import java.util.HashMap;
import java.util.Map;

public class ClientMessageCanvasInsert extends ClientMessage {
    private final Map<CanvasObjectType, CanvasInsert[]> canvasInsertsMap = new HashMap<>();

    public ClientMessageCanvasInsert(ByteBuf byteBuf) {
        super(ClientMessageType.CANVAS_INSERT);
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            CanvasObjectType type = CanvasObjectType.valueOf(byteBuf.readUnsignedByte());
            CanvasInsert[] canvasInserts = new CanvasInsert[byteBuf.readUnsignedByte()];
            for (int j = 0; j < canvasInserts.length; j++) {
                canvasInserts[j] = new CanvasInsert(type, byteBuf);
            }
            canvasInsertsMap.put(type, canvasInserts);
        }
    }

    public Map<CanvasObjectType, CanvasInsert[]> getCanvasInsertsMap() {
        return canvasInsertsMap;
    }
}
