package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateException;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;

public class CanvasUpdateMove extends CanvasUpdate {
    private final CanvasObject canvasObject;

    public CanvasUpdateMove(ByteBuf byteBuf) {
        super(byteBuf);
        canvasObject = new CanvasObject(byteBuf);
    }

    protected CanvasUpdateType getCanvasUpdateType() {
        return CanvasUpdateType.MOVE;
    }

    @Override
    public void update(Canvas canvas, short id) throws CanvasUpdateException {
        CanvasObjectWrapper canvasObjectWrapper = canvas.getCanvasObjects().get(id);
        if (canvasObjectWrapper == null) {
            throw new CanvasUpdateException("CanvasObject does not exist");
        }

        canvasObjectWrapper.getCanvasObject().update(canvasObject);
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        canvasObject.serialize(byteBuf);
    }
}
