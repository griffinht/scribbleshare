package net.stzups.board.protocol.server;

import net.stzups.board.room.User;

public class ServerPacketAddUser extends ServerPacketUser {
    public ServerPacketAddUser(User user) {
        super(ServerPacketType.ADD_CLIENT, user);
    }
}
