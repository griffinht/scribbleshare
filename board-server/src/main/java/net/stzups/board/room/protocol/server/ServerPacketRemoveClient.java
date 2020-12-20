package net.stzups.board.room.protocol.server;

import net.stzups.board.room.Client;

public class ServerPacketRemoveClient extends ServerPacketId {
    public ServerPacketRemoveClient(Client client) {
        super(ServerPacketType.REMOVE_CLIENT, client.getId());
    }
}
