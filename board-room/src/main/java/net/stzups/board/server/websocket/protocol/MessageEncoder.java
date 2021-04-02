package net.stzups.board.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;

import java.util.List;

/**
 * Encodes a ServerPacket sent by the server to
 */
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<List<ServerMessage>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, List<ServerMessage> serverMessages, ByteBuf b) {
        System.out.println("encoding " + serverMessages.size());
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        ByteBuf byteBuf = binaryWebSocketFrame.content();
        for (ServerMessage serverMessage : serverMessages) {
            System.out.println("send " + serverMessage.getClass().getSimpleName());
            serverMessage.serialize(byteBuf);
        }
        ctx.writeAndFlush(binaryWebSocketFrame);
    }
}
