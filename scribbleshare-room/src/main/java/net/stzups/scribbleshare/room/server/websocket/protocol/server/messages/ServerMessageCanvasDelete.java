package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.CanvasDelete;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageCanvasDelete extends ServerMessage {
    private final CanvasDelete[] canvasDeletes;

    public ServerMessageCanvasDelete(CanvasDelete[] canvasDeletes) {
        super(ServerMessageType.CANVAS_DELETE);
        this.canvasDeletes = canvasDeletes;
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) canvasDeletes.length);
        for (CanvasDelete canvasDelete : canvasDeletes) {
            canvasDelete.serialize(byteBuf);
        }
    }
}
