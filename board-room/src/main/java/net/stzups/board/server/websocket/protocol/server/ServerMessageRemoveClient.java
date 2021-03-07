package net.stzups.board.server.websocket.protocol.server;

import net.stzups.board.server.websocket.Client;

public class ServerMessageRemoveClient extends ServerMessageClient {
    public ServerMessageRemoveClient(Client client) {
        super(ServerMessageType.REMOVE_CLIENT, client);
    }
}
