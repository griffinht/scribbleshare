package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageCanvasUpdate extends ServerMessage {
    private final CanvasUpdate[] canvasUpdates;

    public ServerMessageCanvasUpdate(CanvasUpdate[] canvasUpdates) {
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
        for (CanvasUpdate canvasUpdate : canvasUpdates) {
            canvasUpdate.serialize(byteBuf);
        }
    }
}
