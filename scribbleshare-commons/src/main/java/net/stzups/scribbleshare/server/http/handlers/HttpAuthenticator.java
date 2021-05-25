package net.stzups.scribbleshare.server.http.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.exception.exceptions.FailedException;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticatedUserSession;
import net.stzups.scribbleshare.data.objects.authentication.AuthenticationResult;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSession;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpUserSessionCookie;
import net.stzups.scribbleshare.data.objects.authentication.http.PersistentHttpUserSession;
import net.stzups.scribbleshare.server.http.exception.HttpException;
import net.stzups.scribbleshare.server.http.exception.exceptions.BadRequestException;
import net.stzups.scribbleshare.server.http.exception.exceptions.InternalServerException;
import net.stzups.scribbleshare.server.http.exception.exceptions.NotFoundException;
import net.stzups.scribbleshare.server.http.exception.exceptions.UnauthorizedException;

import java.util.List;
import java.util.logging.Level;

import static net.stzups.scribbleshare.server.http.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpAuthenticator extends MessageToMessageDecoder<FullHttpRequest> {
    private static final AttributeKey<AuthenticatedUserSession> USER = AttributeKey.valueOf(HttpAuthenticator.class, "USER");
    public static AuthenticatedUserSession getUser(ChannelHandlerContext ctx) {
        return ctx.channel().attr(USER).get();
    }

    private final ScribbleshareDatabase httpSessionDatabase;
    private final String uri;

    public HttpAuthenticator(ScribbleshareDatabase httpSessionDatabase) {
        this(httpSessionDatabase, null);
    }

    public HttpAuthenticator(ScribbleshareDatabase httpSessionDatabase, String uri) {
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

        AuthenticatedUserSession user = ctx.channel().attr(USER).get();
        if (user != null) {
            return;
        }

        ctx.channel().attr(USER).set(authenticateHttpUserSession(request, httpSessionDatabase));
    }

    /** authenticates, or null if no authentication */
    public static AuthenticatedUserSession authenticateHttpUserSession(FullHttpRequest request, ScribbleshareDatabase database) throws UnauthorizedException, InternalServerException {
        HttpSessionCookie sessionCookie;
        try {
            sessionCookie = HttpUserSessionCookie.getHttpUserSessionCookie(request);
        } catch (BadRequestException e) {
            throw new UnauthorizedException("Malformed cookie", e);
        }

        if (sessionCookie == null) {
            return null;
        }

        HttpUserSession userSession = database.getHttpSession(sessionCookie);
        if (userSession == null) {
            throw new UnauthorizedException("Bad authentication (bad id)");
        }

        AuthenticationResult result = userSession.validate(sessionCookie);
        if (result != AuthenticationResult.SUCCESS) {
            throw new UnauthorizedException("Bad authentication " + result);
        }

        User user = database.getUser(userSession.getUser());
        if (user == null)
            throw new InternalServerException("Unknown user for authentication");

        return new AuthenticatedUserSession(user);
    }

    /** create session and persistent session for user */
    private static AuthenticatedUserSession createHttpSession(User user, HttpConfig httpConfig, ScribbleshareDatabase database, HttpHeaders httpHeaders) throws InternalServerException {
        // create session
        HttpUserSession httpUserSession = new HttpUserSession(httpConfig, user, httpHeaders);
        try {
            database.addHttpSession(httpUserSession);
        } catch (FailedException e) {
            throw new InternalServerException("Failed to add http session", e);
        }

        // create new persistent session
        PersistentHttpUserSession a = new PersistentHttpUserSession(httpConfig, httpUserSession, httpHeaders);
        try {
            database.addPersistentHttpUserSession(a);
        } catch (FailedException e) {
            throw new InternalServerException("Failed to add persistent http session", e);
        }

        return new AuthenticatedUserSession(user);
    }

    /** logs in if not authenticated, or null if no auth */
    public static AuthenticatedUserSession logInHttpSession(FullHttpRequest request, ScribbleshareDatabase database, HttpHeaders httpHeaders, HttpConfig httpConfig) throws UnauthorizedException, InternalServerException {
        AuthenticatedUserSession session = authenticateHttpUserSession(request, database);
        if (session != null) {
            return session;
        }

        HttpSessionCookie sessionCookie;
        try {
            sessionCookie = HttpUserSessionCookie.getHttpUserSessionCookie(request);
        } catch (BadRequestException e) {
            throw new UnauthorizedException("Malformed cookie", e);
        }
        if (sessionCookie == null) {
            return null;
        }

        PersistentHttpUserSession persistentHttpUserSession = database.getPersistentHttpUserSession(sessionCookie);
        if (persistentHttpUserSession == null) {
            throw new UnauthorizedException("Bad authentication (bad id)");
        }

        try {
            database.expirePersistentHttpUserSession(persistentHttpUserSession);
        } catch (FailedException e) {
            throw new InternalServerException("Failed to expire persistent http session", e);
        }

        AuthenticationResult result = persistentHttpUserSession.validate(sessionCookie);
        if (result != AuthenticationResult.SUCCESS) {
            throw new UnauthorizedException("Bad authentication " + result);
        }
        // now logged in

        User user = database.getUser(persistentHttpUserSession.getId());
        if (user == null) {
            throw new InternalServerException("User somehow does not exist " + persistentHttpUserSession.getUser());
        }

        return createHttpSession(user, httpConfig, database, httpHeaders);
    }

    /** creates new user if not logged in or authenticated */
    public static AuthenticatedUserSession createHttpSession(FullHttpRequest request, ScribbleshareDatabase database, HttpConfig httpConfig, HttpHeaders httpHeaders) throws UnauthorizedException, InternalServerException {
        AuthenticatedUserSession session = logInHttpSession(request, database, httpHeaders, httpConfig);
        if (session != null) {
            return session;
        }

        User user = new User();
        try {
            database.addUser(user);
        } catch (FailedException e) {
            throw new InternalServerException("Failed to add user", e);
        }

        return createHttpSession(user, httpConfig, database, httpHeaders);
    }
}
