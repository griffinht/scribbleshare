package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;

public class CanvasUpdateInsert extends CanvasUpdate {
    private final CanvasObjectWrapper canvasObjectWrapper;

    public CanvasUpdateInsert(ByteBuf byteBuf) {
        super(byteBuf);
        this.canvasObjectWrapper = new CanvasObjectWrapper(byteBuf);
    }

    @Override
    protected CanvasUpdateType getCanvasUpdateType() {
        return CanvasUpdateType.INSERT;
    }

    @Override
    public void update(Canvas canvas, short id) {
        if (canvas.getCanvasObjects().putIfAbsent(id, canvasObjectWrapper) != null) {
            throw new RuntimeException("CanvasObject already exists");
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        canvasObjectWrapper.serialize(byteBuf);
    }
}
