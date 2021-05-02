package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdates;

import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageCanvasUpdate extends ClientMessage {
    private final CanvasUpdates[] canvasUpdatesArray;

    public ClientMessageCanvasUpdate(ByteBuf byteBuf) throws DeserializationException {
        canvasUpdatesArray = new CanvasUpdates[byteBuf.readUnsignedByte()];
        for (int i = 0; i < canvasUpdatesArray.length; i++) {
            canvasUpdatesArray[i] = new CanvasUpdates(byteBuf);
        }
    }

    @Override
    public ClientMessageType getMessageType() {
        return ClientMessageType.CANVAS_UPDATE;
    }

    public CanvasUpdates[] getCanvasUpdatesArray() {
        return canvasUpdatesArray;
    }
}
