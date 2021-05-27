package net.stzups.scribbleshare.server.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.handler.RequestHandler;
import net.stzups.scribbleshare.server.http.objects.Route;

import java.net.InetSocketAddress;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

public class HealthcheckRequestHandler extends RequestHandler {
    public HealthcheckRequestHandler() {
        super("/healthcheck");
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request, Route route) throws HttpException {
        if (request.uri().equals("/healthcheck")) {
            if (!((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().isLoopbackAddress()) {
                throw new NotFoundException("Healthcheck request from address which is not a loopback address");
            } else {
                send(ctx, request, HttpResponseStatus.OK);
                Scribbleshare.getLogger(ctx).info("Good healthcheck response");
                return;
            }
        }
    }
}
