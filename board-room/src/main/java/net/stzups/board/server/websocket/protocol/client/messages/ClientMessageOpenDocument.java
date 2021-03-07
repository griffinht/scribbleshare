package net.stzups.board.server.websocket.protocol.client.messages;

import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageOpenDocument extends ClientMessage {
    private long id;

    public ClientMessageOpenDocument(long id) {
        super(ClientMessageType.OPEN_DOCUMENT);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
