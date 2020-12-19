package net.stzups.board.room.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.board.room.protocol.packets.Packet;
import net.stzups.board.room.protocol.packets.PacketDraw;
import net.stzups.board.room.protocol.packets.PacketOffsetDraw;

@ChannelHandler.Sharable
public class PacketEncoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf b) {
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        ByteBuf byteBuf = binaryWebSocketFrame.content();
        byteBuf.writeByte(packet.getPacketType().getId());
        switch (packet.getPacketType()) {
            case OFFSET_DRAW:
                PacketOffsetDraw packetOffsetDraw = (PacketOffsetDraw) packet;
                byteBuf.writeShort(packetOffsetDraw.getOffsetX());
                byteBuf.writeShort(packetOffsetDraw.getOffsetY());
                break;
            case DRAW:
                PacketDraw packetDraw = (PacketDraw) packet;
                byteBuf.writeShort(packetDraw.getX());
                byteBuf.writeShort(packetDraw.getY());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported packet type " + packet + " while encoding");
        }
        ctx.writeAndFlush(binaryWebSocketFrame);
    }
}
