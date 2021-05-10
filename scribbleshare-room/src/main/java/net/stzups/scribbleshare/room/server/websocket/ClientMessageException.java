package net.stzups.scribbleshare.room.server.websocket;

import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;

public class ClientMessageException extends Exception {
    public ClientMessageException(ClientMessage clientMessage, String message) {
        super("Handling " + clientMessage.getClass().getSimpleName() + " caused " + message);
    }

    public ClientMessageException(ClientMessage clientMessage, Throwable cause) {
        super(clientMessage.getClass().getSimpleName(), cause);
    }
}
