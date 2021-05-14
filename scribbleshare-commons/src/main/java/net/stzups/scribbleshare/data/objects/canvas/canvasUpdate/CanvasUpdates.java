package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationLengthException;

public class CanvasUpdates {
    private final short id;
    private final CanvasUpdate[] canvasUpdates;

    public CanvasUpdates(ByteBuf byteBuf) throws DeserializationException {
        id = byteBuf.readShort();
        canvasUpdates = new CanvasUpdate[byteBuf.readUnsignedByte()];
        if (canvasUpdates.length == 0) throw new DeserializationLengthException(canvasUpdates, 0);
        for (int i = 0; i < canvasUpdates .length; i++) {
            CanvasUpdateType type = CanvasUpdateType.deserialize(byteBuf);
            canvasUpdates[i] = CanvasUpdate.deserialize(type, byteBuf);
        }
    }

    public short getId() {
        return id;
    }

    public void update(Canvas canvas) throws CanvasUpdateException {
        for (CanvasUpdate canvasUpdate : canvasUpdates) {
            try {
                canvasUpdate.update(canvas, id);
            } catch (CanvasUpdateException e) {
                throw new CanvasUpdateException("Failed to apply " + canvasUpdate.getCanvasUpdateType() + " to CanvasObject with id " + id, e);
            }
        }
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort(id);
        byteBuf.writeByte((byte) canvasUpdates.length);
        for (CanvasUpdate canvasUpdates : this.canvasUpdates) {
            canvasUpdates.getCanvasUpdateType().serialize(byteBuf);
            canvasUpdates.serialize(byteBuf);
        }
    }
}
