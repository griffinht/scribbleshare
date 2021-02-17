package net.stzups.board.server.websocket.protocol.server;

import net.stzups.board.data.objects.User;

public class ServerPacketAddUser extends ServerPacketUser {
    public ServerPacketAddUser(User user) {
        super(ServerPacketType.ADD_CLIENT, user);
    }
}
