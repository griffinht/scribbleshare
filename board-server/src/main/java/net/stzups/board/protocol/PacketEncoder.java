package net.stzups.board.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.board.protocol.server.ServerPacket;
import net.stzups.board.protocol.server.ServerPacketDraw;
import net.stzups.board.protocol.server.ServerPacketId;
import net.stzups.board.protocol.server.ServerPacketOpenDocument;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Encodes a ServerPacket sent by the server to
 */
@ChannelHandler.Sharable
public class PacketEncoder extends MessageToByteEncoder<List<ServerPacket>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, List<ServerPacket> serverPackets, ByteBuf b) {
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        ByteBuf byteBuf = binaryWebSocketFrame.content();
        for (ServerPacket serverPacket : serverPackets) {
            byteBuf.writeByte((byte) serverPacket.getPacketType().getId()); //todo test with values over 127 (sign issues)
            if (serverPacket instanceof ServerPacketId) {
                byteBuf.writeShort((short) ((ServerPacketId) serverPacket).getId());
            }
            switch (serverPacket.getPacketType()) {
                case ADD_CLIENT:
                case REMOVE_CLIENT:
                    break;
                case DRAW: {
                    ServerPacketDraw packetDraw = (ServerPacketDraw) serverPacket;
                    Point[] points = packetDraw.getPoints();
                    byteBuf.writeShort((short) points.length);
                    for (Point point : points) {
                        byteBuf.writeByte((byte) point.dt);
                        byteBuf.writeShort(point.x);
                        byteBuf.writeShort(point.y);
                    }
                    break;
                }
                case OPEN_DOCUMENT: {
                    ServerPacketOpenDocument serverPacketOpenDocument = (ServerPacketOpenDocument) serverPacket;
                    System.out.println(serverPacketOpenDocument.getDocument().getId() + ", " + serverPacketOpenDocument.getDocument().getName());
                    writeString(serverPacketOpenDocument.getDocument().getId(), byteBuf);
                    writeString(serverPacketOpenDocument.getDocument().getName(), byteBuf);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unsupported packet type " + serverPacket + " while encoding");
            }
        }
        ctx.writeAndFlush(binaryWebSocketFrame);
    }

    private void writeString(String string, ByteBuf byteBuf) {
        if (string.length() > 0xff) {
            throw new UnsupportedOperationException("String too long");
        }
        byte[] buffer = string.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeByte((byte) buffer.length);
        byteBuf.writeBytes(buffer);
    }
}
