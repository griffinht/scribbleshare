package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.objects.Points;
import net.stzups.board.server.websocket.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Canvas {
    private Map<CanvasObjectType, List<CanvasObject>> canvasObjectsMap = new HashMap<>();
    private Map<CanvasObjectType, List<CanvasObject>> updatedCanvasObjectMap = new HashMap<>();

    public Canvas(ByteBuf byteBuf) {
        for (int i = 0; i < byteBuf.readShort(); i++) {
            CanvasObjectType canvasObjectType = CanvasObjectType.valueOf(byteBuf.readShort());
            switch (canvasObjectType) {
                case POINTS:
                    List<CanvasObject> objects = updatedCanvasObjectMap.computeIfAbsent(canvasObjectType, k -> new ArrayList<>());//get or initialize
                    for (int j = 0; j < byteBuf.readShort(); i++) {
                        objects.add(new Points(byteBuf));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown canvas object type ");
            }
        }
        for (Map.Entry<CanvasObjectType, List<CanvasObject>> canvasObjects : canvasObjectsMap.entrySet()) {
            byteBuf.writeByte((byte) canvasObjects.getKey().getId());
            byteBuf.writeShort(canvasObjects.getValue().size());
            for (CanvasObject canvasObject : canvasObjects.getValue()) {
                canvasObject.serialize(byteBuf);
            }
        }
    }

    public void update(Client client, Canvas canvas) {//canvas will be discarded
        for (Map.Entry<CanvasObjectType, List<CanvasObject>> entry : canvas.updatedCanvasObjectMap.entrySet()) {
            updatedCanvasObjectMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
        }
    }

    public void serialize(ByteBuf byteBuf, Map<CanvasObjectType, List<CanvasObject>> canvasObjectsMap) {
        byteBuf.writeShort((short) canvasObjectsMap.size());
        for (Map.Entry<CanvasObjectType, List<CanvasObject>> canvasObjects : canvasObjectsMap.entrySet()) {
            byteBuf.writeShort((short) canvasObjects.getKey().getId());
            byteBuf.writeShort(canvasObjects.getValue().size());
            for (CanvasObject canvasObject : canvasObjects.getValue()) {
                canvasObject.serialize(byteBuf);
            }
        }
    }

    public void serialize(ByteBuf byteBuf) {
        serialize(byteBuf, this.canvasObjectsMap);
    }

    public void serializeUpdated(ByteBuf byteBuf) {
        serialize(byteBuf, this.updatedCanvasObjectMap);
        for (Map.Entry<CanvasObjectType, List<CanvasObject>> entry : updatedCanvasObjectMap.entrySet()) {
            canvasObjectsMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
            entry.getValue().clear();//will be reused
        }
    }
}
