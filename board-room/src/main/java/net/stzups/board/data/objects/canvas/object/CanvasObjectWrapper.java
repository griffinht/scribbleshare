package net.stzups.board.data.objects.canvas.object;

import io.netty.buffer.ByteBuf;

public class CanvasObjectWrapper {
    private byte dt;
    private CanvasObject canvasObject;

    public CanvasObjectWrapper(CanvasObjectType canvasObjectType, ByteBuf byteBuf) {
        dt = byteBuf.readByte();
        canvasObject = CanvasObject.getCanvasObject(canvasObjectType, byteBuf);
    }

    public CanvasObject getCanvasObject() {
        return canvasObject;
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(dt);
        canvasObject.serialize(byteBuf);
    }
}
