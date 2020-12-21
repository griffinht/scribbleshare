package net.stzups.board.protocol.server;

import net.stzups.board.room.Client;

public class ServerPacketAddClient extends ServerPacketId {
    public ServerPacketAddClient(Client client) {
        super(ServerPacketType.ADD_CLIENT, client.getId());
    }
}
