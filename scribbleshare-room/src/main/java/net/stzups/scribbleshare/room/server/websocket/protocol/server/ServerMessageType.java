package net.stzups.scribbleshare.room.server.websocket.protocol.server;

public enum ServerMessageType {
    ADD_CLIENT(0),
    REMOVE_CLIENT(1),
    UPDATE_CANVAS(2),
    UPDATE_DOCUMENT(3),
    ADD_USER(4),
    DELETE_DOCUMENT(5),
    GET_INVITE(6),
    OPEN_DOCUMENT(7)
    ;

    private final int id;

    ServerMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
