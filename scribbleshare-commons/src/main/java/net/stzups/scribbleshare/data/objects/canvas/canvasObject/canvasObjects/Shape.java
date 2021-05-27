package net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects;

import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.scribbleshare.data.objects.canvas.Color;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.EntityCanvasObject;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationTypeException;
import net.stzups.scribbleshare.util.DebugString;

import java.util.EnumSet;
import java.util.Map;

public class Shape extends EntityCanvasObject {
    private enum Type {
        RECTANGLE(0),
        ELLIPSE(1),
        TRIANGLE(2),
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

        public static Type deserialize(ByteBuf byteBuf) throws DeserializationException {
            int id = byteBuf.readUnsignedByte();
            Type objectType = objectTypeMap.get(id);
            if (objectType == null) {
                throw new DeserializationTypeException(Shape.class, id);
            }
            return objectType;
        }
    }

    private final Type type;
    private final Color color;

    public Shape(ByteBuf byteBuf) throws DeserializationException {
        super(byteBuf);
        this.type = Type.deserialize(byteBuf);
        color = new Color(byteBuf);
    }

    @Override
    public CanvasObjectType getCanvasObjectType() {
        return CanvasObjectType.SHAPE;
    }

    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) type.id);
        color.serialize(byteBuf);
    }

    @Override
    public String toString() {
        return DebugString.get(Shape.class, super.toString())
                .add("type", type)
                .add("color", color)
                .toString();
    }
}
