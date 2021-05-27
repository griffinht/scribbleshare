package net.stzups.scribbleshare.backend.server.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.objects.Form;

public abstract class FormHandler {
    private final String route;

    protected FormHandler(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }

    public abstract void handle(ChannelHandlerContext ctx, FullHttpRequest request, Form form) throws BadRequestException, InternalServerException;
}
