package net.stzups.scribbleshare.server.http.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;

import java.net.InetSocketAddress;
import java.util.List;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpHandler extends MessageToMessageDecoder<FullHttpRequest> {
    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) {
        Scribbleshare.getLogger(ctx).info(request.method() + " " + request.uri());

        if (request.decoderResult().isFailure()) {
            send(ctx, request, HttpResponseStatus.BAD_REQUEST);
            Scribbleshare.getLogger(ctx).info("Bad request");
            return;
        }

        if (request.uri().equals("/healthcheck")) {
            if (!((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().isLoopbackAddress()) {
                send(ctx, request, HttpResponseStatus.NOT_FOUND);
                Scribbleshare.getLogger(ctx).warning("Healthcheck request from address which is not a loopback address");
            } else {
                send(ctx, request, HttpResponseStatus.OK);
                Scribbleshare.getLogger(ctx).info("Good healthcheck response");
            }

            return;
        }

        out.add(request.retain());
    }
}
