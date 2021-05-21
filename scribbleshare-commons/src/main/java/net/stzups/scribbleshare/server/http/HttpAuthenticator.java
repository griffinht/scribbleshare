package net.stzups.scribbleshare.server.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;

import java.util.List;

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
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        if (uri != null && !request.uri().equals(uri)) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
            Scribbleshare.getLogger(ctx).warning("Bad uri");
            return;
        }

        Long user = ctx.channel().attr(USER).get();
        if (user != null) {
            out.add(request.retain());
            return;
        }

        HttpSessionCookie cookie = HttpSessionCookie.getHttpSessionCookie(request, HttpUserSession.COOKIE_NAME);
        if (cookie != null) {
            HttpUserSession httpSession = httpSessionDatabase.getHttpSession(cookie);
            if (httpSession != null && httpSession.validate(cookie)) {
                Scribbleshare.getLogger(ctx).info("Authenticated with id " + httpSession.getUser());
                ctx.channel().attr(USER).set(httpSession.getUser());
                out.add(request.retain());
            } else {
                Scribbleshare.getLogger(ctx).warning("Bad authentication");
                //bad authentication attempt
                //todo rate limit timeout server a proper response???
                send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
            }
        } else {
            Scribbleshare.getLogger(ctx).info("No authentication");
            send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
        }
    }
}
