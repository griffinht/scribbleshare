package net.stzups.scribbleshare.data.objects.canvas.canvasObject;

import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationTypeException;

import java.util.EnumSet;
import java.util.Map;

public enum CanvasObjectType {
    SHAPE(0),
    IMAGE(1),
    MOUSE(2),
    LINE(3),
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

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) id);
    }

    public static CanvasObjectType deserialize(ByteBuf byteBuf) throws DeserializationException {
        int id = byteBuf.readUnsignedByte();
        CanvasObjectType objectType = objectTypeMap.get(id);
        if (objectType == null) {
            throw new DeserializationTypeException(CanvasObjectType.class, id);
        }
        return objectType;
    }
}
