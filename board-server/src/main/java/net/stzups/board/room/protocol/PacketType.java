package net.stzups.board.room.protocol;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum PacketType {
    CLOSE(0),
    DRAW(1),
    OFFSET_DRAW(2),
    NEW_CLIENT(3),
    ;

    private static Map<Integer, PacketType> packetTypeMap = new IntObjectHashMap<>();
    static {
        for (PacketType packetType : EnumSet.allOf(PacketType.class)) {
            packetTypeMap.put(packetType.id, packetType);
        }
    }

    private int id;

    PacketType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PacketType valueOf(int id) {
        PacketType packetType = packetTypeMap.get(id);
        if (packetType == null) {
            throw new IllegalArgumentException("Unknown PacketType for given id " + id);
        }
        return packetType;
    }
}
