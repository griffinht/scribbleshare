package net.stzups.board.data.objects.canvas.object.wrappers;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObject;

public class CanvasObjectsWrapper extends CanvasObjectWrapper {
    private CanvasObject[] canvasObjects;

    public CanvasObjectsWrapper(ByteBuf byteBuf) {
        super(byteBuf);
        canvasObjects = new CanvasObject[byteBuf.readUnsignedByte()];
        for (int i = 0; i < canvasObjects.length; i++) {
            canvasObjects[i] = CanvasObject.getCanvasObject(getCanvasObjectType(), byteBuf);
        }
    }

    public CanvasObject[] getCanvasObjects() {
        return canvasObjects;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) canvasObjects.length);
        for (CanvasObject canvasObjects : canvasObjects) {
            canvasObjects.serialize(byteBuf);
        }
    }
}
