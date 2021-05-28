package net.stzups.scribbleshare.server.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.handler.HttpHandler;

import java.net.InetSocketAddress;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

public class HealthcheckRequestHandler extends HttpHandler {
    public HealthcheckRequestHandler() {
        super("/healthcheck");
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        if (request.uri().equals("/healthcheck")) {
            if (!((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().isLoopbackAddress()) {
                throw new NotFoundException("Healthcheck request from address which is not a loopback address");
            } else {
                send(ctx, request, HttpResponseStatus.OK);
                return true;
            }
        }

        return false;
    }
}
