package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.wrappers.CanvasObjectsStateWrapper;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

import java.util.Map;

public class ClientMessageUpdateDocument extends ClientMessage {
    private Map<Short, CanvasObjectsStateWrapper> map;

    public ClientMessageUpdateDocument(ByteBuf byteBuf) {
        super(ClientMessageType.UPDATE_DOCUMENT);
        for (int i = 0; i < byteBuf.readUnsignedShort(); i++) {
            CanvasObjectsStateWrapper canvasObjectsStateWrapper = new CanvasObjectsStateWrapper(byteBuf);
            map.put(canvasObjectsStateWrapper.getId(), canvasObjectsStateWrapper);
        }
    }

    public Map<Short, CanvasObjectsStateWrapper> getMap() {
        return map;
    }
}
