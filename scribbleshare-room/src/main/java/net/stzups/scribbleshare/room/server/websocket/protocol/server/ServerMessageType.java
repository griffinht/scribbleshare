package net.stzups.scribbleshare.room.server.websocket.protocol.server;

public enum ServerMessageType {
    ADD_CLIENT(0),
    REMOVE_CLIENT(1),
    UPDATE_DOCUMENT(2),
    ADD_USER(3),
    DELETE_DOCUMENT(4),
    GET_INVITE(5),
    OPEN_DOCUMENT(6),
    CANVAS_INSERT(7),
    CANVAS_DELETE(8),
    CANVAS_MOVE(9),
    ;

    private final int id;

    ServerMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
