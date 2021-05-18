package net.stzups.scribbleshare.server.http;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.stzups.scribbleshare.Scribbleshare;

@ChannelHandler.Sharable
public class DebugLog extends ChannelDuplexHandler {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Scribbleshare.getLogger(ctx).info("Connection opened");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Scribbleshare.getLogger(ctx).info("Connection closed");
        ctx.fireChannelInactive();
    }
}
