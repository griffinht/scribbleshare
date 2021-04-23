package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateDelete;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateInsert;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateMouseMove;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateMove;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageCanvasUpdate extends ClientMessage {
    private final CanvasUpdate[] canvasUpdates;

    public ClientMessageCanvasUpdate(ByteBuf byteBuf) {
        super(ClientMessageType.CANVAS_UPDATE);
        canvasUpdates = new CanvasUpdate[byteBuf.readUnsignedByte()];
        for (int i = 0; i < canvasUpdates.length; i++) {
            CanvasUpdate canvasUpdate;
            switch (CanvasUpdateType.valueOf(byteBuf.readUnsignedByte())) {
                case INSERT:
                    canvasUpdate = new CanvasUpdateInsert(byteBuf);
                    break;
                case MOVE:
                    canvasUpdate = new CanvasUpdateMove(byteBuf);
                    break;
                case DELETE:
                    canvasUpdate = new CanvasUpdateDelete(byteBuf);
                    break;
                case MOUSEMOVE:
                    canvasUpdate = new CanvasUpdateMouseMove(byteBuf);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown ClientMessageType ");
            }
            canvasUpdates[i] = canvasUpdate;
        }
    }

    public CanvasUpdate[] getCanvasUpdates() {
        return canvasUpdates;
    }
}
