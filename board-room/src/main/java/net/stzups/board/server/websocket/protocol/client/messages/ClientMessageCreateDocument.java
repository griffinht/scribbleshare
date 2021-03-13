package net.stzups.board.server.websocket.protocol.client.messages;

import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageCreateDocument extends ClientMessage {
    public ClientMessageCreateDocument() {
        super(ClientMessageType.CREATE_DOCUMENT);
    }
}
