package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectWrapper;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientMessageUpdateCanvas extends ClientMessage {
    private final Map<CanvasObjectType, Map<Short, CanvasObjectWrapper[]>> canvas = new HashMap<>();

    public ClientMessageUpdateCanvas(ByteBuf byteBuf) {
        super(ClientMessageType.UPDATE_CANVAS);
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            CanvasObjectType canvasObjectType = CanvasObjectType.valueOf(byteBuf.readUnsignedByte());
            Map<Short, CanvasObjectWrapper[]> map = new HashMap<>();
            int lengthJ = byteBuf.readUnsignedShort();
            for (int j = 0; j < lengthJ; j++) {
                short id = byteBuf.readShort();
                CanvasObjectWrapper[] canvasObjectWrappers = new CanvasObjectWrapper[byteBuf.readUnsignedByte()];
                for (int k = 0; k < canvasObjectWrappers.length; k++) {
                    canvasObjectWrappers[k] = new CanvasObjectWrapper(canvasObjectType, byteBuf);
                }
                map.put(id, canvasObjectWrappers);
            }
            canvas.put(canvasObjectType, map);
        }
    }

    public Map<CanvasObjectType, Map<Short, CanvasObjectWrapper[]>> getCanvasObjects() {
        return canvas;
    }
}
