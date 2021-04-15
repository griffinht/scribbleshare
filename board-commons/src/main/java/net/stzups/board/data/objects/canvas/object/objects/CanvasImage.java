package net.stzups.board.data.objects.canvas.object.objects;

import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.board.data.objects.canvas.object.CanvasObject;

import java.util.EnumSet;
import java.util.Map;

public class CanvasImage extends CanvasObject {
    private enum Type {
        PNG(0),
        URL(1),
        ;

        private static final Map<Integer, Type> messageTypeMap = new IntObjectHashMap<>();
        static {
            for (Type messageType : EnumSet.allOf(Type.class)) {
                messageTypeMap.put(messageType.id, messageType);
            }
        }

        public final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type valueOf(int id) {
            Type messageType = messageTypeMap.get(id);
            if (messageType == null) {
                throw new IllegalArgumentException("Unknown CanvasImage type for given id " + id);
            }
            return messageType;
        }
    }

    private final Type type;
    private final byte[] data;

    public CanvasImage(ByteBuf byteBuf) {
        super(byteBuf);
        this.type = Type.valueOf(byteBuf.readUnsignedByte());
        data = byteBuf.readBytes((int) byteBuf.readUnsignedInt()).array();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte((byte) type.id);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
