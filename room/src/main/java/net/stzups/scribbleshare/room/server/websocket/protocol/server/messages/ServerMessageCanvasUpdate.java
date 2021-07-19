package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdates;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;
import net.stzups.util.DebugString;

public class ServerMessageCanvasUpdate extends ServerMessage {
    private final CanvasUpdates[] canvasUpdates;

    public ServerMessageCanvasUpdate(CanvasUpdates[] canvasUpdates) {
        this.canvasUpdates = canvasUpdates;
    }

    @Override
    protected ServerMessageType getMessageType() {
        return ServerMessageType.CANVAS_UPDATE;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) canvasUpdates.length);
        for (CanvasUpdates canvasUpdates : canvasUpdates) {
            canvasUpdates.serialize(byteBuf);
        }
    }

    @Override
    public String toString() {
        return DebugString.get(ServerMessageCanvasUpdate.class)
                .add("canvasUpdates", canvasUpdates)
                .toString();
    }
}
