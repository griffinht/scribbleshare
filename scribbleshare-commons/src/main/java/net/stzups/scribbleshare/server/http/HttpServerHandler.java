package net.stzups.scribbleshare.server.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.httphandler.HttpHandler;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
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
        throw new NotFoundException("No " + HttpHandler.class + " for request");
    }
}