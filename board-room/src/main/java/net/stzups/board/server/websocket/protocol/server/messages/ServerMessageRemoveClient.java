package net.stzups.board.server.websocket.protocol.server.messages;

import net.stzups.board.server.websocket.Client;
import net.stzups.board.server.websocket.protocol.server.ServerMessageClient;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageRemoveClient extends ServerMessageClient {
    public ServerMessageRemoveClient(Client client) {
        super(ServerMessageType.REMOVE_CLIENT, client);
    }
}
