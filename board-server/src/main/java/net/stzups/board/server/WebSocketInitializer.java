package net.stzups.board.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.board.room.PacketHandler;

@ChannelHandler.Sharable
public class WebSocketInitializer extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            ctx.pipeline().addLast(new PacketHandler());
        }
    }
}
