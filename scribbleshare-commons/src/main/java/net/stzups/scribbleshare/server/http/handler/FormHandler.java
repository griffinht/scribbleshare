package net.stzups.scribbleshare.server.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.objects.Form;

public abstract class FormHandler extends HttpHandler {
    protected FormHandler(String route) {
        super(route);
    }

    @Override
    public final boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        if (request.method().equals(HttpMethod.POST)) {
            return handle(ctx, request, new Form(request));
        }

        return false;
    }

    public abstract boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, Form form) throws HttpException;
}
