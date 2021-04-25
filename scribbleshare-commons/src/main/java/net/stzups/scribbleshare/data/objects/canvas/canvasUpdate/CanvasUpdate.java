package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;

public abstract class CanvasUpdate {
    private final CanvasUpdateType canvasObjectType;

    public CanvasUpdate(CanvasUpdateType canvasUpdateType) {
        this.canvasObjectType = canvasUpdateType;
    }

    public abstract void update(Canvas canvas);

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) canvasObjectType.getId());
    }
}
