package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.canvasUpdates;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.Canvas;
import net.stzups.scribbleshare.data.objects.canvas.CanvasObjectWrapper;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateType;

public class CanvasUpdateDelete extends CanvasUpdate {
    private final byte dt;
    private final short id;

    public CanvasUpdateDelete(ByteBuf byteBuf) {
        super(CanvasUpdateType.DELETE);
        this.dt = byteBuf.readByte();
        this.id = byteBuf.readShort();
    }

    @Override
    public void update(Canvas canvas) {
        CanvasObjectWrapper canvasObjectWrapper = canvas.getCanvasObjects().remove(id);
        if (canvasObjectWrapper == null) {
            System.out.println("Tried to delete object that does not exist"); //todo
        }
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte(dt);
        byteBuf.writeShort(id);
    }
}
