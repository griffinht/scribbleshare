package net.stzups.board.room.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.board.room.protocol.server.ServerPacket;
import net.stzups.board.room.protocol.server.ServerPacketDraw;
import net.stzups.board.room.protocol.server.ServerPacketId;
import net.stzups.board.room.protocol.server.ServerPacketOffsetDraw;

/**
 * Encodes a ServerPacket sent by the server to
 */
@ChannelHandler.Sharable
public class PacketEncoder extends MessageToByteEncoder<ServerPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ServerPacket serverPacket, ByteBuf b) {
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        ByteBuf byteBuf = binaryWebSocketFrame.content();
        byteBuf.writeByte(serverPacket.getPacketType().getId());
        if (serverPacket instanceof ServerPacketId) {
            byteBuf.writeByte(((ServerPacketId) serverPacket).getId());
        }
        switch (serverPacket.getPacketType()) {
            case OFFSET_DRAW:
                ServerPacketOffsetDraw packetOffsetDraw = (ServerPacketOffsetDraw) serverPacket;
                byteBuf.writeShort(packetOffsetDraw.getOffsetX());
                byteBuf.writeShort(packetOffsetDraw.getOffsetY());
                break;
            case DRAW:
                ServerPacketDraw packetDraw = (ServerPacketDraw) serverPacket;
                byteBuf.writeShort(packetDraw.getX());
                byteBuf.writeShort(packetDraw.getY());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported packet type " + serverPacket + " while encoding");
        }
        ctx.writeAndFlush(binaryWebSocketFrame);
    }
}
