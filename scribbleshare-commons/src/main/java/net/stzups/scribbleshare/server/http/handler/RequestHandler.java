package net.stzups.scribbleshare.server.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.objects.Route;

public abstract class RequestHandler extends HttpHandler {
    protected RequestHandler(String route) {
        super(route);
    }

    @Override
    public final boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        return handle(ctx, request, new Route(request.uri()));
    }

    public abstract boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, Route route) throws HttpException;
}
