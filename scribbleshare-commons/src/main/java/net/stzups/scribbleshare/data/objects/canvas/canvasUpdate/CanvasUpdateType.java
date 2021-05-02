package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.util.collection.IntObjectHashMap;
import net.stzups.scribbleshare.data.exceptions.DeserializationTypeException;

import java.util.EnumSet;
import java.util.Map;

public enum CanvasUpdateType {
    INSERT(0),
    MOVE(1),
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

    public static CanvasUpdateType deserialize(int id) throws DeserializationTypeException {
        CanvasUpdateType messageType = messageTypeMap.get(id);
        if (messageType == null) {
            throw new DeserializationTypeException(CanvasUpdateType.class, id);
        }
        return messageType;
    }
}
