package net.stzups.board.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageCreateDocument extends ClientMessage {
    public ClientMessageCreateDocument(ByteBuf byteBuf) {
        super(ClientMessageType.CREATE_DOCUMENT);
    }
}
