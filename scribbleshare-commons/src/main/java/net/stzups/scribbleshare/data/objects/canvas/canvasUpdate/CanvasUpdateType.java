package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum CanvasUpdateType {
    MOVE(0),
    INSERT(1),
    DELETE(2),
    ;

    private static final Map<Integer, CanvasUpdateType> messageTypeMap = new IntObjectHashMap<>();
    static {
        for (CanvasUpdateType messageType : EnumSet.allOf(CanvasUpdateType.class)) {
            messageTypeMap.put(messageType.id, messageType);
        }
    }

    private final int id;

    CanvasUpdateType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CanvasUpdateType valueOf(int id) {
        CanvasUpdateType messageType = messageTypeMap.get(id);
        if (messageType == null) {
            throw new IllegalArgumentException("Unknown PacketType for given id " + id);
        }
        return messageType;
    }
}
