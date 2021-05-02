package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateDelete;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateInsert;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateMove;

public abstract class CanvasUpdate {
    private final CanvasUpdateType canvasObjectType;

    public CanvasUpdate(CanvasUpdateType canvasUpdateType) {
        this.canvasObjectType = canvasUpdateType;
    }

    public abstract void update(Canvas canvas);

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) canvasObjectType.getId());
    }

    static CanvasUpdate getCanvasUpdate(ByteBuf byteBuf) {
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
            default:
                throw new IllegalArgumentException("Unknown CanvasUpdate type");
        }
        return canvasUpdate;
    }
}
