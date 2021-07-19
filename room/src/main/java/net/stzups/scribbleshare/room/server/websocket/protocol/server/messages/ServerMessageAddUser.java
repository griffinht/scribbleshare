package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;
import net.stzups.util.DebugString;

public class ServerMessageAddUser extends ServerMessage {
    private final User user;

    public ServerMessageAddUser(User user) {
        this.user = user;
    }

    @Override
    protected ServerMessageType getMessageType() {
        return ServerMessageType.ADD_USER;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(user.getId());
    }

    @Override
    public String toString() {
        return DebugString.get(ServerMessageAddUser.class)
                .add("user", user)
                .toString();
    }
}
