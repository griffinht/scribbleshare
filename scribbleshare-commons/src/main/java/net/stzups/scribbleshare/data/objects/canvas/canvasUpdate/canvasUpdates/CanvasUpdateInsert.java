package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;

import java.util.HashMap;
import java.util.Map;

public class CanvasUpdateInsert extends CanvasUpdate {
    private final byte dt;
    private final short id;
    private final CanvasObjectWrapper canvasObjectWrapper;

    public CanvasUpdateInsert(ByteBuf byteBuf) {
        super(CanvasUpdateType.INSERT);
        this.dt = byteBuf.readByte();
        this.id = byteBuf.readShort();
        this.canvasObjectWrapper = new CanvasObjectWrapper(byteBuf);
    }

    @Override
    public void update(Canvas canvas) {
        canvas.getCanvasObjects().put(id, canvasObjectWrapper);
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte(dt);
        byteBuf.writeShort(id);
        canvasObjectWrapper.serialize(byteBuf);
    }
}
