package net.stzups.board.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.board.server.websocket.WebSocketHandshakeHandler;

@ChannelHandler.Sharable
public class WebSocketInitializer extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            ctx.channel().attr(ServerInitializer.LOGGER).get().info("WebSocket connection initialized");
            ctx.pipeline().remove(this);
            ctx.pipeline().addLast(new WebSocketHandshakeHandler());//todo give this a different executor https://stackoverflow.com/questions/49133447/how-can-you-safely-perform-blocking-operations-in-a-netty-channel-handler
        }
    }
}
