package net.stzups.scribbleshare.server.http;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;

import java.util.logging.Level;

@ChannelHandler.Sharable
public class HttpExceptionHandler extends ChannelDuplexHandler {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        Scribbleshare.getLogger(ctx).log(Level.WARNING, "Uncaught exception", throwable);
        HttpUtils.send(ctx, null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        //todo ctx.close();
    }
}
