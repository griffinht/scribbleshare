package net.stzups.board.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.board.server.ServerInitializer;
import net.stzups.board.server.websocket.protocol.server.ServerMessage;

import java.util.List;
import java.util.logging.Logger;

/**
 * Encodes a ServerPacket sent by the server to
 */
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<List<ServerMessage>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, List<ServerMessage> serverMessages, ByteBuf b) {
        Logger logger = ctx.channel().attr(ServerInitializer.LOGGER).get();

        StringBuilder stringBuilder = new StringBuilder();
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        ByteBuf byteBuf = binaryWebSocketFrame.content();
        for (ServerMessage serverMessage : serverMessages) {
            stringBuilder.append(serverMessage.getClass().getSimpleName()).append(", ");
            serverMessage.serialize(byteBuf);
        }
        ctx.channel().attr(ServerInitializer.LOGGER).get().info("send " + stringBuilder.toString());
        ctx.writeAndFlush(binaryWebSocketFrame);
    }
}
