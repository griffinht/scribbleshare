package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageAddUser extends ServerMessage {
    private final User user;

    public ServerMessageAddUser(User user) {
        super(ServerMessageType.ADD_USER);
        this.user = user;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeLong(user.getId());
    }
}
