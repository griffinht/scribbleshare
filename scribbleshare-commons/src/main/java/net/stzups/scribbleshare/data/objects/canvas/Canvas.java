package net.stzups.scribbleshare.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectWrapper;

import java.util.HashMap;
import java.util.Map;

public class Canvas {
    // a serialized blank canvas
    public static final ByteBuf EMPTY_CANVAS;
    static {
        Canvas canvas = new Canvas();
        EMPTY_CANVAS = Unpooled.buffer();
        canvas.serialize(EMPTY_CANVAS);
    }

    private final Map<CanvasObjectType, Map<Short, CanvasObject>> canvasObjects = new HashMap<>();

    public Canvas() {
    }

    /**
     * Deserializes canvas from db
     */
    public Canvas(ByteBuf byteBuf) {
        int length = byteBuf.readUnsignedByte();
        for (int i = 0; i < length; i++) {
            CanvasObjectType canvasObjectType = CanvasObjectType.valueOf(byteBuf.readUnsignedByte());
            Map<Short, CanvasObject> map = new HashMap<>();
            canvasObjects.put(canvasObjectType, map);
            int l = byteBuf.readUnsignedShort();
            for (int j = 0; j < l; j++) {
                map.put(byteBuf.readShort(), CanvasObject.getCanvasObject(canvasObjectType, byteBuf));
            }
        }
    }

    public void update(Map<CanvasObjectType, Map<Short, CanvasObjectWrapper>> updateCanvasObjects) {
        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObjectWrapper>> entry : updateCanvasObjects.entrySet()) {
            Map<Short, CanvasObject> map = canvasObjects.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
            for (Map.Entry<Short, CanvasObjectWrapper> entry1 : entry.getValue().entrySet()) {
                map.put(entry1.getKey(), entry1.getValue().getCanvasObject());
            }
        }
    }

    public void delete(Map<CanvasObjectType, Map<Short, CanvasObject>> deleteCanvasObjects) {
        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObject>> entry : deleteCanvasObjects.entrySet()) {
            Map<Short, CanvasObject> map = canvasObjects.get(entry.getKey());
            if (map == null) {
                throw new UnsupportedOperationException("Tried to delete canvas object that does not exist");//todo exceptions
            } else {
                for (Map.Entry<Short, CanvasObject> entry1 : entry.getValue().entrySet()) {
                    map.remove(entry1.getKey());
                }
            }
        }
    }

    public void serialize(ByteBuf byteBuf) {
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
