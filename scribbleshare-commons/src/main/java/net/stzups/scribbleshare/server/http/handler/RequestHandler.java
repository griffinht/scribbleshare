package net.stzups.scribbleshare.server.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;

public abstract class RequestHandler extends HttpHandler {
    private final String referer;

    protected RequestHandler(HttpConfig config, String referer, String route) {
        super(route);
        this.referer = config.getOrigin() + referer;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        String referer = request.headers().get(HttpHeaderNames.REFERER);
        if (referer != null && !referer.equals(this.referer)) {
            throw new UnauthorizedException("Bad referer for " + getRoute() + ", got " + HttpHeaderNames.REFERER + ": " + referer + ", should have been referred from " + this.referer);
        }

        handleRequest(ctx, request);
        return true;
    }

    protected abstract void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException;
}
