package net.stzups.board.server.websocket.protocol.server;

import net.stzups.board.data.objects.User;

public class ServerPacketRemoveUser extends ServerPacketUser {
    public ServerPacketRemoveUser(User user) {
        super(ServerPacketType.REMOVE_CLIENT, user);
    }
}
