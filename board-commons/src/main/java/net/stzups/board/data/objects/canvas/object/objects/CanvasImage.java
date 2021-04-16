package net.stzups.board.data.objects.canvas.object.objects;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObject;

public class CanvasImage extends CanvasObject {
    private final long id;

    public CanvasImage(ByteBuf byteBuf) {
        super(byteBuf);
        this.id = byteBuf.readLong();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(id);
    }
}
