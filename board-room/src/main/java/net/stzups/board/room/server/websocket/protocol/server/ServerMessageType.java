package net.stzups.board.room.server.websocket.protocol.server;

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

    private final int id;

    ServerMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
