package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObject;
import net.stzups.board.data.objects.canvas.object.CanvasObjectType;
import net.stzups.board.data.objects.canvas.object.CanvasObjectWrapper;

import java.util.HashMap;
import java.util.Map;

public class Canvas {
    private Map<CanvasObjectType, Map<Short, CanvasObject>> canvasObjects = new HashMap<>();

    public Canvas() {

    }

    /**
     * Deserializes canvas from db
     */
    public Canvas(ByteBuf byteBuf) {
        for (int i = 0; i < byteBuf.readUnsignedByte(); i++) {
            CanvasObjectType canvasObjectType = CanvasObjectType.valueOf(byteBuf.readUnsignedByte());
            Map<Short, CanvasObject> map = new HashMap<>();
            canvasObjects.put(canvasObjectType, map);
            for (int j = 0; j < byteBuf.readUnsignedShort(); j++) {
                map.put(byteBuf.readShort(), CanvasObject.getCanvasObject(canvasObjectType, byteBuf));
            }
        }
    }

    public void update(Map<CanvasObjectType, Map<Short, CanvasObjectWrapper>> updateCanvasObjects) {
        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObjectWrapper>> entry : updateCanvasObjects.entrySet()) {
            Map<Short, CanvasObject> map = canvasObjects.get(entry.getKey());
            if (map == null) {
                map = new HashMap<>();
            }
            for (Map.Entry<Short, CanvasObjectWrapper> entry1 : entry.getValue().entrySet()) {
                map.put(entry1.getKey(), entry1.getValue().getCanvasObject());
            }
        }
    }

    public void delete(Canvas canvas) {
        delete(canvas.canvasObjects);
    }

    public void delete(Map<CanvasObjectType, Map<Short, CanvasObject>> deleteCanvasObjects) {
        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObject>> entry : deleteCanvasObjects.entrySet()) {
            Map<Short, CanvasObject> map = canvasObjects.get(entry.getKey());
            if (map == null) {
                System.out.println("cant delete " + entry.getKey() + ", its already gone");
            } else {
                for (Map.Entry<Short, CanvasObject> entry1 : entry.getValue().entrySet()) {
                    map.remove(entry1.getKey());
                }
            }
        }
    }

    public void clear() {
        canvasObjects.clear();
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

    public boolean isEmpty() {
        return canvasObjects.isEmpty();
    }
}
