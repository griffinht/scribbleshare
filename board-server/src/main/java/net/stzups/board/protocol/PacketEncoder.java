package net.stzups.board.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.board.protocol.server.ServerPacket;

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
            System.out.println("send " + serverPacket.getClass().getSimpleName());
            serverPacket.serialize(byteBuf);
        }
        ctx.writeAndFlush(binaryWebSocketFrame);
    }
}
