package net.stzups.scribbleshare.server.http.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;

import java.util.List;
import java.util.logging.Level;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpAuthenticator extends MessageToMessageDecoder<FullHttpRequest> {
    private static final AttributeKey<Long> USER = AttributeKey.valueOf(HttpAuthenticator.class, "USER");
    public static Long getUser(ChannelHandlerContext ctx) {
        return ctx.channel().attr(USER).get();
    }

    private final HttpSessionDatabase httpSessionDatabase;
    private final String uri;

    public HttpAuthenticator(HttpSessionDatabase httpSessionDatabase) {
        this(httpSessionDatabase, null);
    }

    public HttpAuthenticator(HttpSessionDatabase httpSessionDatabase, String uri) {
        this.httpSessionDatabase = httpSessionDatabase;
        this.uri = uri;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) {
        try {
            handle(ctx, request);
        } catch (HttpException e) {
            Scribbleshare.getLogger(ctx).log(Level.WARNING, "Exception while handling HTTP request", e);
            send(ctx, request, e.responseStatus());
        }
        out.add(request.retain());
    }

    private void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        if (uri != null && !request.uri().equals(uri)) {
            throw new NotFoundException("Bad uri");
        }

        Long user = ctx.channel().attr(USER).get();
        if (user != null) {
            return;
        }

        HttpSessionCookie cookie = HttpSessionCookie.getHttpSessionCookie(request, HttpUserSession.COOKIE_NAME);
        if (cookie != null) {
            HttpUserSession httpSession = httpSessionDatabase.getHttpSession(cookie);
            if (httpSession != null && httpSession.validate(cookie)) {
                Scribbleshare.getLogger(ctx).info("Authenticated with id " + httpSession.getUser());
                ctx.channel().attr(USER).set(httpSession.getUser());
            } else {
                //bad authentication attempt
                //todo rate limit timeout server a proper response???
                throw new UnauthorizedException("Bad authentication");
            }
        } else {
            throw new UnauthorizedException("No authentication");
        }
    }
}
