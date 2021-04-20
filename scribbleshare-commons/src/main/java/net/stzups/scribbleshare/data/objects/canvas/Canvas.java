package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;

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

    private final Map<Short, CanvasObjectWrapper> canvasObjects = new HashMap<>();
    private boolean dirty = false;

    public Canvas() {
        dirty = true;
    }

    /**
     * Deserializes canvas from db
     */
    public Canvas(ByteBuf byteBuf) {
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            CanvasObjectType canvasObjectType = CanvasObjectType.valueOf(byteBuf.readUnsignedByte());
            int l = byteBuf.readUnsignedShort();
            for (int j = 0; j < l; j++) {
                canvasObjects.put(byteBuf.readShort(), new CanvasObjectWrapper(canvasObjectType, CanvasObject.getCanvasObject(canvasObjectType, byteBuf)));
            }
        }
    }

    public boolean isDirty() {
        boolean dirty = this.dirty;
        this.dirty = false;
        return dirty;
    }

    public void insert(Map<CanvasObjectType, CanvasInsert[]> canvasInsertsMap) {
        dirty = true;
        for (Map.Entry<CanvasObjectType, CanvasInsert[]> entry : canvasInsertsMap.entrySet()) {
            for (CanvasInsert canvasInsert : entry.getValue()) {
                canvasObjects.put(canvasInsert.getId(), new CanvasObjectWrapper(entry.getKey(), canvasInsert.getCanvasObject()));
            }
        }
    }

    public void move(Map<Short, CanvasMove[]> canvasMovesMap) {
        dirty = true;
        for (Map.Entry<Short, CanvasMove[]> entry : canvasMovesMap.entrySet()) {
            CanvasObjectWrapper canvasObjectWrapper = canvasObjects.get(entry.getKey());
            if (canvasObjectWrapper == null) {
                continue;
            }

            canvasObjectWrapper.getCanvasObject().update(entry.getValue()[entry.getValue().length - 1].getCanvasObject());
        }
    }

    public void delete(CanvasDelete[] canvasDeletes) {
        dirty = true;
        for (CanvasDelete canvasDelete : canvasDeletes) {
            CanvasObjectWrapper canvasObjectWrapper = canvasObjects.remove(canvasDelete.getId());
            if (canvasObjectWrapper == null) {
                System.out.println("Tried to delete object that does not exist"); //todo
            }
        }
    }

    public void serialize(ByteBuf byteBuf) {
        Map<CanvasObjectType, Map<Short, CanvasObject>> canvasObjects = new HashMap<>();

        for (Map.Entry<Short, CanvasObjectWrapper> entry : this.canvasObjects.entrySet()) {
            Map<Short, CanvasObject> map = canvasObjects.computeIfAbsent(entry.getValue().getType(), (c -> new HashMap<>()));
            map.put(entry.getKey(), entry.getValue().getCanvasObject());
        }

        byteBuf.writeByte((byte) canvasObjects.size());
        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObject>> entry : canvasObjects.entrySet()) {
            byteBuf.writeByte((byte) entry.getKey().getId());
            byteBuf.writeShort((short) entry.getValue().size());
            for (Map.Entry<Short, CanvasObject> entry1 : entry.getValue().entrySet()) {
                byteBuf.writeShort(entry1.getKey());
                entry1.getValue().serialize(byteBuf);
            }
        }
    }

    @Override
    public String toString() {
        return "Canvas{canvasObjects=" + canvasObjects.size() + "}";
    }
}
