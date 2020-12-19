package net.stzups.board.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.board.Board;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) frame;
            String text = textWebSocketFrame.text();
            Board.getLogger().info(ctx.channel().remoteAddress() + " sent " + text);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(text));
        } else {
            throw new UnsupportedOperationException("Unsupported frame type " + frame.getClass().getName());
        }
    }
}
