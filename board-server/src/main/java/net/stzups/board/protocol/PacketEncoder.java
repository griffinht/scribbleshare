package net.stzups.board.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.board.protocol.server.ServerPacket;
import net.stzups.board.protocol.server.ServerPacketDraw;
import net.stzups.board.protocol.server.ServerPacketId;

/**
 * Encodes a ServerPacket sent by the server to
 */
@ChannelHandler.Sharable
public class PacketEncoder extends MessageToByteEncoder<ServerPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ServerPacket serverPacket, ByteBuf b) {
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        ByteBuf byteBuf = binaryWebSocketFrame.content();
        byteBuf.writeByte((byte) serverPacket.getPacketType().getId()); //todo test with values over 127 (sign issues)
        if (serverPacket instanceof ServerPacketId) {
            byteBuf.writeShort((short) ((ServerPacketId) serverPacket).getId());
        }
        switch (serverPacket.getPacketType()) {
            case ADD_CLIENT:
            case REMOVE_CLIENT:
                break;
            case DRAW:
                ServerPacketDraw packetDraw = (ServerPacketDraw) serverPacket;
                Point[] points = packetDraw.getPoints();
                byteBuf.writeByte((byte) points.length);
                for (Point point : points) {
                    byteBuf.writeByte((byte) point.dt);
                    byteBuf.writeShort(point.x);
                    byteBuf.writeShort(point.y);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported packet type " + serverPacket + " while encoding");
        }
        ctx.writeAndFlush(binaryWebSocketFrame);
    }
}
