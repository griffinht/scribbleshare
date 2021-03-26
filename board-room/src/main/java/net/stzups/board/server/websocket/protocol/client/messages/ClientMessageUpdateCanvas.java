package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObject;
import net.stzups.board.data.objects.canvas.object.CanvasObjectType;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

import java.util.HashMap;
import java.util.Map;

public class ClientMessageUpdateCanvas extends ClientMessage {
    private Map<CanvasObjectType, Map<Short, CanvasObject>> canvas = new HashMap<>();

    public ClientMessageUpdateCanvas(ByteBuf byteBuf) {
        super(ClientMessageType.UPDATE_CANVAS);
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            CanvasObjectType canvasObjectType = CanvasObjectType.valueOf(byteBuf.readUnsignedByte());
            Map<Short, CanvasObject> map = new HashMap<>();
            int lengthJ = byteBuf.readUnsignedShort();
            for (int j = 0; j < lengthJ; j++) {
                map.put(byteBuf.readShort(), CanvasObject.getCanvasObject(canvasObjectType, byteBuf));
            }
            canvas.put(canvasObjectType, map);
        }
    }

    public Map<CanvasObjectType, Map<Short, CanvasObject>> getCanvasObjects() {
        return canvas;
    }
}
