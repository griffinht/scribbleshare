package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;

public class CanvasObjectWrapper {
    private final CanvasObjectType type;
    private final CanvasObject canvasObject;

    public CanvasObjectWrapper(CanvasObjectType type, CanvasObject canvasObject) {
        this.type = type;
        this.canvasObject = canvasObject;
    }

    public CanvasObjectWrapper(ByteBuf byteBuf) throws DeserializationException {
        this.type = CanvasObjectType.deserialize(byteBuf.readUnsignedByte());
        this.canvasObject = CanvasObject.deserialize(type, byteBuf);
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) type.getId());
        canvasObject.serialize(byteBuf);
    }

    CanvasObjectType getType() {
        return type;
    }

    public CanvasObject getCanvasObject() {
        return canvasObject;
    }
}
