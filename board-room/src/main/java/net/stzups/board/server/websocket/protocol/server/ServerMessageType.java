package net.stzups.board.server.websocket.protocol.server;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum ServerMessageType {
    ADD_CLIENT(0),
    REMOVE_CLIENT(1),
    DRAW(2),
    OPEN_DOCUMENT(3),
    ADD_DOCUMENT(4),
    HANDSHAKE(5),
    ADD_USER(6)
    ;

    private static Map<Integer, ServerMessageType> packetTypeMap = new IntObjectHashMap<>();
    static {
        for (ServerMessageType packetType : EnumSet.allOf(ServerMessageType.class)) {
            packetTypeMap.put(packetType.id, packetType);
        }
    }

    private int id;

    ServerMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ServerMessageType valueOf(int id) {
        ServerMessageType packetType = packetTypeMap.get(id);
        if (packetType == null) {
            throw new IllegalArgumentException("Unknown PacketTypeServer for given id " + id);
        }
        return packetType;
    }
}
