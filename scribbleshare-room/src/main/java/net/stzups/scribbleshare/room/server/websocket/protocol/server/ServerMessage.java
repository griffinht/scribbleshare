package net.stzups.scribbleshare.room.server.websocket.protocol.server;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.room.server.websocket.protocol.Message;

import java.nio.charset.StandardCharsets;

/**
 * Represents a packet sent by the server
 */
public abstract class ServerMessage implements Message {

    /** overriding classes need to call this first */
    public void serialize(ByteBuf bytebuf) {
        bytebuf.writeByte((byte) getMessageType().getId());
    }

    protected abstract ServerMessageType getMessageType();

    /** poorly encodes strings as utf 8 preceded by a one byte unsigned length */
    protected static void writeString(String string, ByteBuf byteBuf) {
        if (string.length() > 0xff) {
            throw new UnsupportedOperationException("String too long");
        }
        byte[] buffer = string.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeByte((byte) buffer.length);
        byteBuf.writeBytes(buffer);
    }
}
