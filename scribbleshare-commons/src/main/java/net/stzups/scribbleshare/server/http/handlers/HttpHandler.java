package net.stzups.scribbleshare.server.http.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Level;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpHandler extends MessageToMessageDecoder<FullHttpRequest> {
    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) {
        try {
            if (!handle(ctx, request)) {
                out.add(request.retain());
            }
        } catch (HttpException e) {
            Scribbleshare.getLogger(ctx).log(Level.WARNING, "Exception while handling HTTP request", e);
            send(ctx, request, e.responseStatus());
        }
    }

    private boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        Scribbleshare.getLogger(ctx).info(request.method() + " " + request.uri());

        if (request.decoderResult().isFailure())
            throw new BadRequestException("Bad request");

        if (request.uri().equals("/healthcheck")) {
            if (!((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().isLoopbackAddress()) {
                throw new NotFoundException("Healthcheck request from address which is not a loopback address");
            } else {
                send(ctx, request, HttpResponseStatus.OK);
                Scribbleshare.getLogger(ctx).info("Good healthcheck response");
                return true;
            }
        }

        return false;
    }
}
