package net.stzups.scribbleshare.server.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
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
    public final boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws HttpException {
        if (!request.method().equals(HttpMethod.POST)) {
            return false;
        }

        String referer = request.headers().get(HttpHeaderNames.REFERER);
        if (referer != null && !referer.equals(this.referer)) {
            throw new UnauthorizedException("Bad referer " + HttpHeaderNames.REFERER + ": " + referer + ", should have been referred from " + this.referer);
        }


        handleRequest(ctx, request, response);
        return true;
    }

    protected abstract void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws HttpException;
}
