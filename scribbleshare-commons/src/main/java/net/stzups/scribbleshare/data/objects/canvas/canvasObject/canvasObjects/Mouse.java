package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;

public class Mouse extends CanvasObject {
    private final short client;

    public Mouse(ByteBuf byteBuf) {
        super(byteBuf);
        this.client = byteBuf.readShort();//todo anyone can move anyone's mouse
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeShort(client);
    }
}
