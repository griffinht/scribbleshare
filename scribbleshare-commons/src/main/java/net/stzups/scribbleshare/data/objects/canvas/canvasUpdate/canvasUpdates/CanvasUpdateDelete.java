package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;

public class CanvasUpdateDelete extends CanvasUpdate {
    private static class CanvasDelete {
        private final byte dt;
        private final short id;

        public CanvasDelete(ByteBuf byteBuf) {
            this.dt = byteBuf.readByte();
            this.id = byteBuf.readShort();
        }

        public void serialize(ByteBuf byteBuf) {
            byteBuf.writeByte(dt);
            byteBuf.writeShort(id);
        }

        public short getId() {
            return id;
        }
    }

    private final CanvasDelete[] canvasDeletes;

    public CanvasUpdateDelete(ByteBuf byteBuf) {
        super(CanvasUpdateType.DELETE);
        canvasDeletes = new CanvasDelete[byteBuf.readUnsignedByte()];
        for (int i = 0; i < canvasDeletes.length; i++) {
            canvasDeletes[i] = new CanvasDelete(byteBuf);
        }
    }

    @Override
    public void update(Canvas canvas) {
        for (CanvasDelete canvasDelete : canvasDeletes) {
            CanvasObjectWrapper canvasObjectWrapper = canvas.getCanvasObjects().remove(canvasDelete.getId());
            if (canvasObjectWrapper == null) {
                System.out.println("Tried to delete object that does not exist"); //todo
            }
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) canvasDeletes.length);
        for (CanvasDelete canvasDelete : canvasDeletes) {
            canvasDelete.serialize(byteBuf);
        }
    }
}
