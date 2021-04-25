package net.stzups.scribbleshare.room.server.websocket.protocol.client;

import io.netty.util.collection.IntObjectHashMap;

import java.util.EnumSet;
import java.util.Map;

public enum ClientMessageType {
    OPEN_DOCUMENT(0),
    CREATE_DOCUMENT(1),
    HANDSHAKE(2),
    DELETE_DOCUMENT(3),
    UPDATE_DOCUMENT(4),
    GET_INVITE(5),
    CANVAS_UPDATE(6),
    MOUSE_MOVE(7),
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
            throw new IllegalArgumentException("Unknown ClientMessageType for given id " + id);
        }
        return messageType;
    }
}
