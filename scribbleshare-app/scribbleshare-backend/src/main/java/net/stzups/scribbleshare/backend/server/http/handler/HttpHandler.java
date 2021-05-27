package net.stzups.scribbleshare.backend.server.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import net.stzups.scribbleshare.server.http.exception.HttpException;

public abstract class HttpHandler {
    private final String route;

    protected HttpHandler(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }

    /**
     * true if the request was handled
     */
    public abstract boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException;
}
