package net.stzups.board.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageGetInvite extends ClientMessage {
    public ClientMessageGetInvite(ByteBuf byteBuf) {
        super(ClientMessageType.GET_INVITE);
    }
}
