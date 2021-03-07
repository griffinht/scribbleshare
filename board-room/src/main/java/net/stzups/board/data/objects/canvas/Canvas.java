package net.stzups.board.data.objects.canvas;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.canvas.objects.Points;
import net.stzups.board.server.websocket.Client;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

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

    public void update(Client client, Canvas canvas) {
        //todo
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort((short) canvasObjectsMap.size());
        for (Map.Entry<CanvasObjectType, List<CanvasObject>> canvasObjects : canvasObjectsMap.entrySet()) {
            byteBuf.writeShort((short) canvasObjects.getKey().getId());
            byteBuf.writeShort(canvasObjects.getValue().size());
            for (CanvasObject canvasObject : canvasObjects.getValue()) {
                canvasObject.serialize(byteBuf);
            }
        }
    }
}
