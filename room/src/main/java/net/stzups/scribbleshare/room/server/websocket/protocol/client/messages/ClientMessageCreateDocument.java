package net.stzups.scribbleshare.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessageType;
import net.stzups.util.D.DebugString;

public class ClientMessageCreateDocument extends ClientMessage {
    public ClientMessageCreateDocument(ByteBuf byteBuf) {

    }

    @Override
    public ClientMessageType getMessageType() {
        return ClientMessageType.CREATE_DOCUMENT;
    }

    @Override
    public String toString() {
        return DebugString.get(ClientMessageCreateDocument.class)
                .toString();
    }
}
