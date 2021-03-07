package net.stzups.board.data.objects.canvas;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum CanvasObjectType {
    POINT(0),
    ;

    private static Map<Integer, CanvasObjectType> objectTypeMap = new IntObjectHashMap<>();
    static {
        for (CanvasObjectType canvasObjectType : EnumSet.allOf(CanvasObjectType.class)) {
            objectTypeMap.put(canvasObjectType.id, canvasObjectType);
        }
    }

    private int id;

    CanvasObjectType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CanvasObjectType valueOf(int id) {
        CanvasObjectType packetType = objectTypeMap.get(id);
        if (packetType == null) {
            throw new IllegalArgumentException("Unknown CanvasObjectType for given id " + id);
        }
        return packetType;
    }
}
