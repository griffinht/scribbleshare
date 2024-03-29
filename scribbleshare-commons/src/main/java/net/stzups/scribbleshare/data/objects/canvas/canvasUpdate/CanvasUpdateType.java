package net.stzups.scribbleshare.data.objects.canvas.canvasUpdate;

import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationTypeException;

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

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) id);
    }

    public static CanvasUpdateType deserialize(ByteBuf byteBuf) throws DeserializationTypeException {
        int id = byteBuf.readUnsignedByte();
        CanvasUpdateType messageType = messageTypeMap.get(id);
        if (messageType == null) {
            throw new DeserializationTypeException(CanvasUpdateType.class, id);
        }
        return messageType;
    }

    @Override
    public String toString() {
        return "CanvasUpdateType{id=" + id + "}";
    }
}
