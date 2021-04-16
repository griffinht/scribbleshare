package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageGetInvite extends ServerMessage {
    private final String code;

    public ServerMessageGetInvite(InviteCode inviteCode) {
        super(ServerMessageType.GET_INVITE);
        code = inviteCode.getCode();
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        writeString(code, byteBuf);
    }
}