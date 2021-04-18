package net.stzups.scribbleshare.room.server.websocket.protocol.client;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum ClientMessageType {
    OPEN_DOCUMENT(0),
    UPDATE_CANVAS(1),
    CREATE_DOCUMENT(2),
    HANDSHAKE(3),
    DELETE_DOCUMENT(4),
    UPDATE_DOCUMENT(5),
    GET_INVITE(6)
    ;

    private static final Map<Integer, ClientMessageType> messageTypeMap = new IntObjectHashMap<>();
    static {
        for (ClientMessageType messageType : EnumSet.allOf(ClientMessageType.class)) {
            messageTypeMap.put(messageType.id, messageType);
        }
    }

    private final int id;

    ClientMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ClientMessageType valueOf(int id) {
        ClientMessageType messageType = messageTypeMap.get(id);
        if (messageType == null) {
            throw new IllegalArgumentException("Unknown PacketType for given id " + id);
        }
        return messageType;
    }
}
