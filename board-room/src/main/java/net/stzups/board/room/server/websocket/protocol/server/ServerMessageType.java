package net.stzups.board.room.server.websocket.protocol.server;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum ServerMessageType {
    ADD_CLIENT(0),
    REMOVE_CLIENT(1),
    UPDATE_CANVAS(2),
    OPEN_DOCUMENT(3),
    UPDATE_DOCUMENT(4),
    ADD_USER(5),
    DELETE_DOCUMENT(6),
    GET_INVITE(7)
    ;

    private static Map<Integer, ServerMessageType> messageTypeMap = new IntObjectHashMap<>();
    static {
        for (ServerMessageType packetType : EnumSet.allOf(ServerMessageType.class)) {
            messageTypeMap.put(packetType.id, packetType);
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
        ServerMessageType messageType = messageTypeMap.get(id);
        if (messageType == null) {
            throw new IllegalArgumentException("Unknown PacketTypeServer for given id " + id);
        }
        return messageType;
    }
}
