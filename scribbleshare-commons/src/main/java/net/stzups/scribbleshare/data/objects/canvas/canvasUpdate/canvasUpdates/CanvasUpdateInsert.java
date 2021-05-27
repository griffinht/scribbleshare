package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateException;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.util.DebugString;

public class CanvasUpdateInsert extends CanvasUpdate {
    private final CanvasObject canvasObject;

    public CanvasUpdateInsert(ByteBuf byteBuf) throws DeserializationException {
        super(byteBuf);
        this.canvasObject = CanvasObject.deserialize(CanvasObjectType.deserialize(byteBuf), byteBuf);
    }

    @Override
    protected CanvasUpdateType getCanvasUpdateType() {
        return CanvasUpdateType.INSERT;
    }

    @Override
    public void update(Canvas canvas, short id) throws CanvasUpdateException {
        if (canvas.getCanvasObjects().putIfAbsent(id, canvasObject) != null) {
            throw new CanvasUpdateException("CanvasObject already exists");
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        canvasObject.getCanvasObjectType().serialize(byteBuf);
        canvasObject.serialize(byteBuf);
    }

    @Override
    public String toString() {
        return DebugString.get(CanvasUpdateInsert.class, super.toString())
                .add("canvasObject", canvasObject)
                .toString();
    }
}
