package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdates;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationLengthException;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;
import net.stzups.scribbleshare.util.DebugString;

public class ClientMessageCanvasUpdate extends ClientMessage {
    private final CanvasUpdates[] canvasUpdatesArray;

    public ClientMessageCanvasUpdate(ByteBuf byteBuf) throws DeserializationException {
        canvasUpdatesArray = new CanvasUpdates[byteBuf.readUnsignedByte()];
        if (canvasUpdatesArray.length == 0) throw new DeserializationLengthException(canvasUpdatesArray, 0);
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

    @Override
    public String toString() {
        return DebugString.get(ClientMessageCanvasUpdate.class)
                .add("canvasUpdatesArray", canvasUpdatesArray)
                .toString();
    }
}
