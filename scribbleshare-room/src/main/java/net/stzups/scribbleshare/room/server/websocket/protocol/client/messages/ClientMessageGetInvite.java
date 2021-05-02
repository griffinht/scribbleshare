package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageGetInvite extends ClientMessage {
    public ClientMessageGetInvite(ByteBuf byteBuf) {

    }

    @Override
    public ClientMessageType getMessageType() {
        return ClientMessageType.GET_INVITE;
    }
}
