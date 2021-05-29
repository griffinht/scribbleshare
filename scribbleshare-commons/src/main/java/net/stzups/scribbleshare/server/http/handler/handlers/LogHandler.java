package net.stzups.scribbleshare.server.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.handler.HttpHandler;

public class LogHandler extends HttpHandler {
    public LogHandler() {
        super("/");
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        Scribbleshare.getLogger(ctx).info(request.method() + " " + request.uri());
        return false;
    }
}
