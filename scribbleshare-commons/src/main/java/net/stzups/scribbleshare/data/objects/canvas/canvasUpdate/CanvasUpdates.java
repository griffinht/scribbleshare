package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;

public class CanvasUpdates {
    private final short id;
    private final CanvasUpdate[][] canvasUpdates;

    public CanvasUpdates(ByteBuf byteBuf) {
        id = byteBuf.readShort();
        canvasUpdates = new CanvasUpdate[byteBuf.readUnsignedByte()][];
        if (canvasUpdates.length == 0) throw new RuntimeException("Length can not be 0");
        for (int i = 0; i < canvasUpdates.length; i++) {
            CanvasUpdateType type = CanvasUpdateType.valueOf(byteBuf.readUnsignedByte());
            CanvasUpdate[] canvasUpdates = new CanvasUpdate[byteBuf.readUnsignedByte()];
            if (canvasUpdates.length == 0) throw new RuntimeException("Length can not be 0");
            for (int k = 0; k < canvasUpdates.length; k++) {
                canvasUpdates[k] = CanvasUpdate.getCanvasUpdate(type, byteBuf);
            }
            this.canvasUpdates[i] = canvasUpdates;
        }
    }

    public short getId() {
        return id;
    }

    public void update(Canvas canvas) {
        for (CanvasUpdate[] canvasUpdates : canvasUpdates) {
            for (CanvasUpdate canvasUpdate : canvasUpdates) {
                try {
                    canvasUpdate.update(canvas, id);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Failed to apply " + canvasUpdate.getCanvasUpdateType() + " to CanvasObject with id " + id, e);
                }
            }
        }
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort(id);
        byteBuf.writeByte((byte) canvasUpdates.length);
        for (CanvasUpdate[] canvasUpdates : this.canvasUpdates) {
            byteBuf.writeByte((byte) canvasUpdates[0].getCanvasUpdateType().getId());
            byteBuf.writeByte((byte) canvasUpdates.length);
            for (CanvasUpdate canvasUpdate : canvasUpdates) {
                canvasUpdate.serialize(byteBuf);
            }
        }
    }
}
