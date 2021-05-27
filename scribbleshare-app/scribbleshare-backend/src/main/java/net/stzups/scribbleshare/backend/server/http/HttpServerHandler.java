package net.stzups.scribbleshare.backend.server.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.backend.server.http.handler.HttpHandler;
import net.stzups.scribbleshare.server.http.exception.HttpException;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    //private static final String AUTHENTICATE_PAGE = "/"; // the page where new users will be automatically created


    private final Queue<HttpHandler> handlers = new ArrayDeque<>();

    public HttpServerHandler() {

    }

    public HttpServerHandler addHandler(HttpHandler handler) {
        handlers.add(handler);
        return this;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            handle(ctx, request);
        } catch (HttpException e) {
            Scribbleshare.getLogger(ctx).log(Level.WARNING, "Exception while handling HTTP request", e);
            send(ctx, request, e.responseStatus());
        }
    }

    private void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        for (HttpHandler handler : handlers) {
            if (request.uri().startsWith(handler.getRoute()) && handler.handle(ctx, request)) {
                return;
            }
        }
        Scribbleshare.getLogger(ctx).warning("No handler for request, serving 404");
        send(ctx, request, HttpResponseStatus.NOT_FOUND);
    }
}