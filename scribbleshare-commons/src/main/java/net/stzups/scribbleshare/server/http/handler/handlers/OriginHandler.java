package net.stzups.scribbleshare.server.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;
import net.stzups.scribbleshare.server.http.handler.HttpHandler;

public class OriginHandler extends HttpHandler {
    private final String origin;

    public OriginHandler(HttpConfig config) {
        super("/");
        this.origin = config.getOrigin();
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        String origin = request.headers().get(HttpHeaderNames.ORIGIN);
        if (origin != null && !origin.equals(this.origin)) {
            throw new UnauthorizedException("Unknown origin " + origin + ", should have been " + this.origin);
        }

        return false;
    }
}
