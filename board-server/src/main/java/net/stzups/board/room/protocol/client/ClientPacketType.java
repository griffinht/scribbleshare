package net.stzups.board.room.protocol.client;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum ClientPacketType {
    OPEN(0),
    DRAW(1),
    OFFSET_DRAW(2),
    ;

    private static Map<Integer, ClientPacketType> packetTypeMap = new IntObjectHashMap<>();
    static {
        for (ClientPacketType packetType : EnumSet.allOf(ClientPacketType.class)) {
            packetTypeMap.put(packetType.id, packetType);
        }
    }

    private int id;

    ClientPacketType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ClientPacketType valueOf(int id) {
        ClientPacketType packetType = packetTypeMap.get(id);
        if (packetType == null) {
            throw new IllegalArgumentException("Unknown PacketType for given id " + id);
        }
        return packetType;
    }
}
