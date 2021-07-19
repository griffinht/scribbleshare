package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateException;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;
import net.stzups.util.DebugString;

public class CanvasUpdateDelete extends CanvasUpdate {
    public CanvasUpdateDelete(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    protected CanvasUpdateType getCanvasUpdateType() {
        return CanvasUpdateType.DELETE;
    }

    @Override
    public void update(Canvas canvas, short id) throws CanvasUpdateException {
        if (canvas.getCanvasObjects().remove(id) == null) {
            throw new CanvasUpdateException("CanvasObject does not exist");
        }
    }

    @Override
    public String toString() {
        return DebugString.get(CanvasUpdateDelete.class, super.toString())
                .toString();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
    }
}
