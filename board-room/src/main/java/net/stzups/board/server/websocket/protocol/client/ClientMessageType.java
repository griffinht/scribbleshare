package net.stzups.board.server.websocket.protocol.client;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum ClientMessageType {
    OPEN_DOCUMENT(0),
    DRAW(1),
    CREATE_DOCUMENT(2),
    HANDSHAKE(3),
    ;

    private static Map<Integer, ClientMessageType> packetTypeMap = new IntObjectHashMap<>();
    static {
        for (ClientMessageType packetType : EnumSet.allOf(ClientMessageType.class)) {
            packetTypeMap.put(packetType.id, packetType);
        }
    }

    private int id;

    ClientMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ClientMessageType valueOf(int id) {
        ClientMessageType packetType = packetTypeMap.get(id);
        if (packetType == null) {
            throw new IllegalArgumentException("Unknown PacketType for given id " + id);
        }
        return packetType;
    }
}
