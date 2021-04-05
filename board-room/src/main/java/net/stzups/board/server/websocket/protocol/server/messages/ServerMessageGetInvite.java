package net.stzups.board.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.data.objects.InviteCode;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;
import net.stzups.board.server.websocket.protocol.server.ServerMessageType;

public class ServerMessageGetInvite extends ServerMessage {
    private String code;

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
