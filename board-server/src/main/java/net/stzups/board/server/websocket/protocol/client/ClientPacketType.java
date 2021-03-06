package net.stzups.board.server.websocket.protocol.client;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum ClientPacketType {
    OPEN_DOCUMENT(0),
    DRAW(1),
    CREATE_DOCUMENT(2),
    HANDSHAKE(3),
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
