package net.stzups.board.room.server.websocket.protocol.client.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.board.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.room.server.websocket.protocol.client.ClientMessageType;

public class ClientMessageHandshake extends ClientMessage {
    private String code;

    public ClientMessageHandshake(ByteBuf byteBuf) {
        super(ClientMessageType.HANDSHAKE);
        this.code = readString(byteBuf);
    }

    public String getCode() {
        return code;
    }
}
