package net.stzups.scribbleshare.server.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;

public abstract class RequestHandler extends HttpHandler {
    private final HttpConfig config;
    private final String referer;

    protected RequestHandler(HttpConfig config, String referer, String route) {
        super(route);
        this.config = config;
        this.referer = referer;
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        String referer = request.headers().get(HttpHeaderNames.REFERER);
        if (referer != null) {
            String string = (config.getSSL() ? "https://" : "http://") + config.getDomain();
            if (!referer.equals(string + this.referer) && !referer.equals(string + ":" + config.getPort() + this.referer)) { // https://example.com/referer or https://example.com:1234/referer
                throw new UnauthorizedException("Bad referer for " + getRoute() + ", got " + HttpHeaderNames.REFERER + ": " + referer + ", should have been referred from " + this.referer);
            }
        }
        handleRequest(ctx, request);
        return true;
    }

    protected abstract void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException;
}
