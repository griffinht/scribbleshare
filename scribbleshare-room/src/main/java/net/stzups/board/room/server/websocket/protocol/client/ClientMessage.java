package net.stzups.board.room.server.websocket.protocol.client;

import io.netty.buffer.ByteBuf;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageCreateDocument;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageGetInvite;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageUpdateCanvas;
import net.stzups.board.room.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;

import java.nio.charset.StandardCharsets;

/**
 * Represents a packet sent by the client
 */
public abstract class ClientMessage {
    private final ClientMessageType packetType;

    protected ClientMessage(ClientMessageType packetType) {
        this.packetType = packetType;
    }

    public ClientMessageType getMessageType() {
        return packetType;
    }

    protected String readString(ByteBuf byteBuf) {
        byte[] buffer = new byte[byteBuf.readUnsignedByte()];
        byteBuf.readBytes(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static ClientMessage getClientMessage(ClientMessageType clientMessageType, ByteBuf byteBuf) {
        ClientMessage message;
        switch (clientMessageType) {
            case OPEN_DOCUMENT:
                message = new ClientMessageOpenDocument(byteBuf);
                break;
            case UPDATE_CANVAS:
                message = new ClientMessageUpdateCanvas(byteBuf);
                break;
            case CREATE_DOCUMENT:
                message = new ClientMessageCreateDocument(byteBuf);
                break;
            case HANDSHAKE:
                message = new ClientMessageHandshake(byteBuf);
                break;
            case DELETE_DOCUMENT:
                message = new ClientMessageDeleteDocument(byteBuf);
                break;
            case UPDATE_DOCUMENT:
                message = new ClientMessageUpdateDocument(byteBuf);
                break;
            case GET_INVITE:
                message = new ClientMessageGetInvite(byteBuf);
                break;
            default:
                throw new IllegalArgumentException("Unsupported message type " + clientMessageType + " while decoding");
        }
        return message;
    }
}
