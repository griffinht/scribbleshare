package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.object.CanvasObject;
import net.stzups.board.data.objects.canvas.object.CanvasObjectType;

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

    public void update(Map<CanvasObjectType, Map<Short, CanvasObject>> updateCanvasObjects) {
        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObject>> entry : updateCanvasObjects.entrySet()) {
            Map<Short, CanvasObject> map = canvasObjects.get(entry.getKey());
            if (map == null) {
                canvasObjects.put(entry.getKey(), entry.getValue());
            } else {
                for (Map.Entry<Short, CanvasObject> entry1 : entry.getValue().entrySet()) {
                    CanvasObject canvasObject = map.replace(entry1.getKey(), entry1.getValue());
                    if (canvasObject != null) {
                        //todo update with new value
                    }
                }
            }
        }
    }

    public void delete(Map<CanvasObjectType, Map<Short, CanvasObject>> deleteCanvasObjects) {
        //Map<CanvasObjectType, Map<Short, CanvasObject>> updatedCanvasObjects = null;

        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObject>> entry : deleteCanvasObjects.entrySet()) {
            Map<Short, CanvasObject> map = canvasObjects.get(entry.getKey());
            if (map == null) {
                System.out.println("cant delete " + entry.getKey() + ", its already gone");
            } else {
                for (Map.Entry<Short, CanvasObject> entry1 : entry.getValue().entrySet()) {
                    CanvasObject canvasObject = map.remove(entry1.getKey());
                    if (canvasObject != null) {
                        /*if (updatedCanvasObjects == null) {//lazy allocation only if there are values to be updated
                            updatedCanvasObjects = new HashMap<>();
                        }*/

                        //todo delete and update
                    }
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
}
