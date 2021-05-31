package net.stzups.scribbleshare.server.http.handler.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.databases.HttpSessionDatabase;
import net.stzups.scribbleshare.data.database.databases.UserDatabase;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;
import net.stzups.scribbleshare.server.http.handler.HttpHandler;

@ChannelHandler.Sharable
public class HttpAuthenticator<T extends UserDatabase & HttpSessionDatabase> extends HttpHandler {
    private static final AttributeKey<AuthenticatedUserSession> USER = AttributeKey.valueOf(HttpAuthenticator.class, "USER");
    public static AuthenticatedUserSession getUser(ChannelHandlerContext ctx) {
        return ctx.channel().attr(USER).get();
    }

    private final T database;
    private final String uri;

    public HttpAuthenticator(T database) {
        this(database, null);
    }

    public HttpAuthenticator(T database, String uri) {
        super("/");
        this.database = database;
        this.uri = uri;
    }

    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponse response) throws HttpException {
        if (uri != null && !request.uri().equals(uri)) {
            throw new NotFoundException("Bad uri");
        }

        AuthenticatedUserSession user = ctx.channel().attr(USER).get();
        if (user != null) {
            return true;
        }

        AuthenticatedUserSession session = HttpUserSession.authenticateHttpUserSession(database, request);
        if (session == null) {
            throw new UnauthorizedException("No authentication");
        }

        Scribbleshare.getLogger(ctx).info("" + session);
        ctx.channel().attr(USER).set(session);
        return true;
    }
}
