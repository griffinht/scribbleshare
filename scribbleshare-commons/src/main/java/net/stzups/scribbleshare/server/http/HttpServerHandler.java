package net.stzups.scribbleshare.server.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.handler.HttpHandler;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpServerHandler extends MessageToMessageDecoder<FullHttpRequest> {
    private final Queue<HttpHandler> handlers = new ArrayDeque<>();

    public HttpServerHandler addLast(HttpHandler handler) {
        handlers.add(handler);
        return this;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) {
        try {
            Scribbleshare.getLogger(ctx).info(request.method() + " " + request.uri());

            if (request.decoderResult().isFailure())
                throw new BadRequestException("Decoding request resulted in " + request.decoderResult());

            for (HttpHandler handler : handlers) {
                if (request.uri().startsWith(handler.getRoute()) && handler.handle(ctx, request)) {
                    return;
                }
            }

            out.add(request.retain());
        } catch (HttpException e) {
            Scribbleshare.getLogger(ctx).log(Level.WARNING, "Exception while handling HTTP request", e);
            send(ctx, request, e.responseStatus());
        }
    }
}