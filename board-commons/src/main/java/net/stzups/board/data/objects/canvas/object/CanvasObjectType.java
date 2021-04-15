package net.stzups.board.data.objects.canvas.object;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum CanvasObjectType {
    SHAPE(0),
    ;

    private static final Map<Integer, CanvasObjectType> objectTypeMap = new IntObjectHashMap<>();
    static {
        for (CanvasObjectType canvasObjectType : EnumSet.allOf(CanvasObjectType.class)) {
            objectTypeMap.put(canvasObjectType.id, canvasObjectType);
        }
    }

    private final int id;

    CanvasObjectType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CanvasObjectType valueOf(int id) {
        CanvasObjectType objectType = objectTypeMap.get(id);
        if (objectType == null) {
            throw new IllegalArgumentException("Unknown CanvasObjectType for given id " + id);
        }
        return objectType;
    }
}
