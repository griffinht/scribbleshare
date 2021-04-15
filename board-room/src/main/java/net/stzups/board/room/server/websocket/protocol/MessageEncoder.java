package net.stzups.board.room.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import net.stzups.board.room.server.websocket.protocol.server.ServerMessage;

import java.util.List;
import java.util.logging.Logger;

/**
 * Encodes a ServerPacket sent by the server to
 */
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<List<ServerMessage>> {
    private final Logger logger;

    public MessageEncoder(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, List<ServerMessage> serverMessages, ByteBuf b) {
        StringBuilder stringBuilder = new StringBuilder();//debug
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        ByteBuf byteBuf = binaryWebSocketFrame.content();
        for (ServerMessage serverMessage : serverMessages) {
            stringBuilder.append(serverMessage.getClass().getSimpleName()).append(", ");//debug
            serverMessage.serialize(byteBuf);
        }
        logger.info("send " + stringBuilder);//debug
        ctx.writeAndFlush(binaryWebSocketFrame);
    }
}
