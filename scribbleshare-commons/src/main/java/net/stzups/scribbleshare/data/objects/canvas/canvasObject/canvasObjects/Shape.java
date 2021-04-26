package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.EntityCanvasObject;

import java.util.EnumSet;
import java.util.Map;

public class Shape extends EntityCanvasObject {
    private enum Type {
        RECTANGLE(0),
        ELLIPSE(1),
        ;

        private static final Map<Integer, Type> objectTypeMap = new IntObjectHashMap<>();
        static {
            for (Type canvasObjectType : EnumSet.allOf(Type.class)) {
                objectTypeMap.put(canvasObjectType.id, canvasObjectType);
            }
        }

        public final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type valueOf(int id) {
            Type objectType = objectTypeMap.get(id);
            if (objectType == null) {
                throw new IllegalArgumentException("Unknown Shape type for given id " + id);
            }
            return objectType;
        }
    }

    private final Type type;
    private final byte red;
    private final byte green;
    private final byte blue;

    public Shape(ByteBuf byteBuf) {
        super(byteBuf);
        this.type = Type.valueOf(byteBuf.readUnsignedByte());
        this.red = byteBuf.readByte();
        this.green = byteBuf.readByte();
        this.blue = byteBuf.readByte();
    }

    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) type.id);
        byteBuf.writeByte(red);
        byteBuf.writeByte(green);
        byteBuf.writeByte(blue);
    }
}
