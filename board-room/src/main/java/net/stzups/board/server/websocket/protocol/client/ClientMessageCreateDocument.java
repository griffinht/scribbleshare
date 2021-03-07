package net.stzups.board.server.websocket.protocol.client;

public class ClientMessageCreateDocument extends ClientMessage {
    public ClientMessageCreateDocument() {
        super(ClientMessageType.CREATE_DOCUMENT);
    }
}
