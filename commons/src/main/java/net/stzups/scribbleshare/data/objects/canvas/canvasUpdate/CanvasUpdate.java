package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateDelete;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateInsert;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates.CanvasUpdateMove;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationTypeException;
import net.stzups.util.DebugString;

public abstract class CanvasUpdate {
    private final byte dt;

    public CanvasUpdate(ByteBuf byteBuf) {
        dt = byteBuf.readByte();
    }

    protected abstract CanvasUpdateType getCanvasUpdateType();

    public abstract void update(Canvas canvas, short id) throws CanvasUpdateException;

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(dt);
    }

    @Override
    public String toString() {
        return DebugString.get(CanvasUpdate.class)
                .add("dt", dt)
                .toString();
    }

    static CanvasUpdate deserialize(CanvasUpdateType type, ByteBuf byteBuf) throws DeserializationException {
        CanvasUpdate canvasUpdate;
        switch (type) {
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
                throw new DeserializationTypeException(CanvasUpdateType.class, type);
        }
        return canvasUpdate;
    }
}
