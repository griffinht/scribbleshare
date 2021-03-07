package net.stzups.board.server.websocket.protocol.server;

import net.stzups.board.server.websocket.Client;

public class ServerPacketRemoveClient extends ServerPacketClient {
    public ServerPacketRemoveClient(Client client) {
        super(ServerPacketType.REMOVE_CLIENT, client);
    }
}
