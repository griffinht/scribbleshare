package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.CanvasDelete;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageCanvasDelete extends ClientMessage {
    private final CanvasDelete[] canvasDeletes;

    public ClientMessageCanvasDelete(ByteBuf byteBuf) {
        super(ClientMessageType.CANVAS_DELETE);
        canvasDeletes = new CanvasDelete[byteBuf.readUnsignedByte()];
        for (int i = 0; i < canvasDeletes.length; i++) {
            canvasDeletes[i] = new CanvasDelete(byteBuf);
        }
    }

    public CanvasDelete[] getCanvasDeletes() {
        return canvasDeletes;
    }
}
