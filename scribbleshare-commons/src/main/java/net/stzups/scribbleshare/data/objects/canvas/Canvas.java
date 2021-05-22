package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdateException;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdates;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationLengthException;

import java.util.HashMap;
import java.util.Map;

public class Canvas {
    // a serialized blank canvas
    private static final ByteBuf EMPTY_CANVAS;
    static {
        Canvas canvas = new Canvas();
        EMPTY_CANVAS = Unpooled.buffer();
        canvas.serialize(EMPTY_CANVAS);
    }
    public static ByteBuf getEmptyCanvas() {
        EMPTY_CANVAS.resetReaderIndex();
        return EMPTY_CANVAS;
    }

    private final Map<Short, CanvasObject> canvasObjects = new HashMap<>();
    private boolean dirty = false;

    public Canvas() {
        dirty = true;
    }

    /**
     * Deserializes canvas from db
     */
    public Canvas(ByteBuf byteBuf) throws DeserializationException {
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            CanvasObjectType type = CanvasObjectType.deserialize(byteBuf);
            int l = byteBuf.readUnsignedShort();
            if (l == 0) throw new DeserializationLengthException(type, 0);
            for (int j = 0; j < l; j++) {
                canvasObjects.put(byteBuf.readShort(), CanvasObject.deserialize(type, byteBuf));
            }
        }
    }

    public Map<Short, CanvasObject> getCanvasObjects() {
        return canvasObjects;
    }

    public boolean isDirty() {
        boolean dirty = this.dirty;
        this.dirty = false;
        return dirty;
    }

    public void update(CanvasUpdates[] canvasUpdatesArray) throws CanvasUpdateException {
        dirty = true;
        for (CanvasUpdates canvasUpdates : canvasUpdatesArray) {
            canvasUpdates.update(this);
        }
    }

    public void serialize(ByteBuf byteBuf) {
        Map<CanvasObjectType, Map<Short, CanvasObject>> canvasObjects = new HashMap<>();

        for (Map.Entry<Short, CanvasObject> entry : this.canvasObjects.entrySet()) {
            Map<Short, CanvasObject> map = canvasObjects.computeIfAbsent(entry.getValue().getCanvasObjectType(), (c -> new HashMap<>()));
            map.put(entry.getKey(), entry.getValue());
        }

        byteBuf.writeByte((byte) canvasObjects.size());
        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObject>> entry : canvasObjects.entrySet()) {
            entry.getKey().serialize(byteBuf);
            byteBuf.writeShort((short) entry.getValue().size());
            for (Map.Entry<Short, CanvasObject> entry1 : entry.getValue().entrySet()) {
                byteBuf.writeShort(entry1.getKey());
                entry1.getValue().serialize(byteBuf);
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Canvas)) {
            return false;
        }

        Canvas canvas = (Canvas) object;
        return canvas.canvasObjects.equals(canvasObjects);
    }

    @Override
    public String toString() {
        return "Canvas{canvasObjects=" + canvasObjects.size() + "}";
    }
}
